package net.azisaba.lgw.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.maps.GameMap;
import net.azisaba.lgw.core.maps.MapContainer;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.teams.DefaultTeamDistributor;
import net.azisaba.lgw.core.teams.TeamDistributor;
import net.md_5.bungee.api.ChatColor;

/**
 *
 * ゲームを司るコアクラス
 * @author siloneco
 *
 */
public class MatchManager {

	// plugin
	private static LeonGunWar plugin;
	private static boolean initialized = false;

	// チーム分けを行うクラス
	private static TeamDistributor teamDistributor;

	// ゲーム中かどうかの判定
	private static boolean isMatching = false;
	// 現在のマップ
	private static GameMap currentMap = null;
	// 試合の残り時間
	private static int timeLeft = 0;
	// 試合を動かすタスク
	private static BukkitTask matchTask;
	// マッチで使用するスコアボード
	private static Scoreboard scoreboard;
	// 赤、青、試合参加エントリー用のスコアボードチーム
	private static Team redTeam, blueTeam, entry;
	// 赤、青チームのチェストプレート
	private static ItemStack redChestPlate, blueChestPlate;

	// 現在の赤チームのポイント (キル数)
	private static int redPoint = 0;
	// 現在の青チームのポイント (キル数)
	private static int bluePoint = 0;

	/**
	 * 初期化メゾッド
	 * Pluginが有効化されたときのみ呼び出されることを前提としています
	 * @param plugin LeonGunWar plugin
	 */
	protected static void init(LeonGunWar plugin) {
		// すでに初期化されている場合はreturn
		if (initialized) {
			return;
		}

		MatchManager.plugin = plugin;

		// デフォルトのTeamDistributorを指定
		MatchManager.teamDistributor = new DefaultTeamDistributor();
		// メインではない新しいスコアボードを取得
		scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();

		// 各スコアボードチームの取得 / 作成 (赤、青、試合参加エントリー用)
		initializeTeams();

		// 各チームのチェストプレートを設定
		// 赤チーム
		redChestPlate = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) redChestPlate.getItemMeta();
		meta.setColor(Color.RED);
		meta.setUnbreakable(true);
		redChestPlate.setItemMeta(meta);

		// 青チーム
		blueChestPlate = new ItemStack(Material.LEATHER_CHESTPLATE);
		meta.setColor(Color.BLUE);
		blueChestPlate.setItemMeta(meta);

		initialized = true;
	}

	/**
	 * マッチを開始するメゾッド
	 *
	 * @exception IllegalStateException すでにゲームがスタートしている場合
	 */
	public static void startMatch() {
		// すでにマッチ中の場合はIllegalStateException
		if (isMatching) {
			throw new IllegalStateException("A match is already started.");
		}

		// マップを抽選
		currentMap = MapContainer.getRandomMap();
		// 参加プレイヤーを取得
		List<Player> entryPlayers = getEntryPlayers();
		// プレイヤーを振り分け
		teamDistributor.distributePlayers(entryPlayers, Arrays.asList(redTeam, blueTeam));

		// 各プレイヤーにチームに沿った処理を行う
		// エントリー削除したときにgetEntries()の中身が変わってエラーを起こさないように新しいリストを作成してfor文を使用する
		// 赤チームの処理
		for (String redEntry : new ArrayList<String>(redTeam.getEntries())) {
			Player p = plugin.getServer().getPlayerExact(redEntry);

			// プレイヤーが見つからない場合はエントリーから削除してcontinue
			if (p == null) {
				redTeam.removeEntry(redEntry);
				continue;
			}

			// メッセージを表示する
			p.sendMessage("あなたは" + ChatColor.DARK_RED + "赤チーム" + ChatColor.RESET + "になりました!");
			// 防具を装備
			p.getInventory().setChestplate(redChestPlate);
			// テレポート
			p.teleport(currentMap.getRedSpawn());
		}

		// 青チームの処理
		for (String blueEntry : new ArrayList<String>(blueTeam.getEntries())) {
			Player p = plugin.getServer().getPlayerExact(blueEntry);

			// プレイヤーが見つからない場合はエントリーから削除してcontinue
			if (p == null) {
				blueTeam.removeEntry(blueEntry);
				continue;
			}

			// メッセージを表示する
			p.sendMessage("あなたは" + ChatColor.BLUE + "青チーム" + ChatColor.RESET + "になりました!");
			// 防具を装備
			p.getInventory().setChestplate(blueChestPlate);
			// テレポート
			p.teleport(currentMap.getBlueSpawn());
		}

		// タスクスタート
		runMatchTask();
	}

	/**
	 * ゲーム終了時に行う処理を書きます
	 */
	public static void finalizeMatch() {
		// 赤チームのEntry削除
		for (String redEntry : new ArrayList<String>(redTeam.getEntries())) {
			redTeam.removeEntry(redEntry);
		}
		// 青チームのEntry削除
		for (String blueEntry : new ArrayList<String>(blueTeam.getEntries())) {
			blueTeam.removeEntry(blueEntry);
		}

		// タスクの終了
		if (matchTask != null) {
			matchTask.cancel();
			matchTask = null;
		}

		// 残り時間を0に
		timeLeft = 0;
	}

	/**
	 * プレイヤーをマッチ参加用のエントリーに参加させます
	 * @param p 参加させたいプレイヤー
	 *
	 * @exception IllegalStateException 試合が行われているときにエントリーしようとした場合
	 */
	public static void entryPlayer(Player p) {
		// ゲーム中の場合はIllegalStateException
		if (isMatching) {
			throw new IllegalStateException("You can't entry while match is started.");
		}

		// すでに参加している場合はreturn
		if (entry.hasEntry(p.getName())) {
			return;
		}

		// エントリー追加
		entry.addEntry(p.getName());
	}

	/**
	 * 試合に参加するプレイヤーのリストを取得します
	 * @return entryスコアボードチームに参加しているプレイヤー
	 */
	private static List<Player> getEntryPlayers() {
		// リスト作成
		List<Player> players = new ArrayList<>();

		// 名前からプレイヤー検索
		for (String entryName : new ArrayList<String>(entry.getEntries())) {
			Player target = plugin.getServer().getPlayerExact(entryName);

			// プレイヤーが見つからない場合はエントリー解除してcontinue
			if (target == null) {
				entry.removeEntry(entryName);
				continue;
			}

			// リストに追加
			players.add(target);
		}

		return players;
	}

	/**
	 * ゲームの残り時間を操作するタイマータスクを起動します
	 * 基本はMatchTimeChangedEventを利用して、イベントからゲームを操作するため
	 * このタスクでは基本他の動作を行いません
	 */
	private static void runMatchTask() {
		// 試合中ならreturn
		if (isMatching) {
			return;
		}

		matchTask = new BukkitRunnable() {
			@Override
			public void run() {
				// matchTimeを減らす
				timeLeft -= 1;

				// イベントを呼び出す
				MatchTimeChangedEvent event = new MatchTimeChangedEvent(timeLeft);
				plugin.getServer().getPluginManager().callEvent(event);

				// 0になったらストップ
				if (timeLeft == 0) {
					this.cancel();
					return;
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	/**
	 * 指定されたチームのプレイヤーリストを取得します
	 * @param team プレイヤーリストを取得したいチーム
	 * @return チームのプレイヤーリスト (BOTHの場合は試合に参加しているすべてのプレイヤー)
	 *
	 * @exception IllegalArgumentException teamがnullの場合
	 */
	public static List<Player> getTeamPlayers(BattleTeam team) {
		// teamがnullならIllegalArgumentException
		if (team == null) {
			throw new IllegalArgumentException("\"team\" mustn't be null.");
		}
		// teamがUNKNOWNなら空のリストを返す
		if (team == BattleTeam.UNKNOWN) {
			return new ArrayList<Player>();
		}

		// リスト作成
		List<Player> players = new ArrayList<>();
		// 名前のリストを作成
		List<String> entryList = null;

		// 赤チームの場合
		if (team == BattleTeam.RED) {
			entryList = new ArrayList<String>(redTeam.getEntries());
		} else if (team == BattleTeam.BLUE) {
			entryList = new ArrayList<String>(blueTeam.getEntries());
		} else if (team == BattleTeam.BOTH) {
			entryList = new ArrayList<String>(redTeam.getEntries());
			entryList.addAll(new ArrayList<String>(blueTeam.getEntries()));
		}

		// Entryしている名前からプレイヤー検索
		for (String entryName : entryList) {
			// プレイヤーを取得
			Player player = plugin.getServer().getPlayerExact(entryName);

			// プレイヤーがいない場合はcontinue
			if (player == null) {
				continue;
			}

			// リストに追加
			players.add(player);
		}

		// 取得したプレイヤーリストを返す
		return players;
	}

	/**
	 * 現在のマップを取得します
	 * 試合が行われていない場合はnullを返します
	 *
	 * @return 試合中のマップ
	 */
	public static GameMap getCurrentGameMap() {
		// 試合中でなかったらnullを返す
		if (!isMatching) {
			return null;
		}

		return currentMap;
	}

	/**
	 * 指定したチームの現在のポイント数を取得します
	 * 試合が行われていない場合は-1を返します
	 *
	 * @param team ポイントを取得したいチーム
	 * @return 指定したチームの現在のポイント
	 *
	 * @exception IllegalArgumentException teamがnullの場合
	 */
	public static int getCurrentTeamPoint(BattleTeam team) {
		// teamがnullならIllegalArgumentException
		if (team == null) {
			throw new IllegalArgumentException("\"team\" mustn't be null.");
		}

		// 試合中でなかったら-1を返す
		if (!isMatching) {
			return -1;
		}

		// 赤チームの場合
		if (team == BattleTeam.RED) {
			return redPoint;
		}

		// 青チームの場合
		if (team == BattleTeam.BLUE) {
			return bluePoint;
		}

		// その他
		return -1;
	}

	/**
	 * 指定したチームに1ポイントを追加します。
	 *
	 * @param team ポイントを追加したいチーム
	 * @exception IllegalArgumentException チームがRED, BLUE以外の場合
	 */
	public static void addTeamPoint(BattleTeam team) {
		// REDでもBLUEでもなければIllegalArgumentException
		if (team != BattleTeam.RED && team != BattleTeam.BLUE) {
			throw new IllegalArgumentException("\"team\" must be RED or BLUE.");
		}

		// REDの場合赤に1ポイント追加
		if (team == BattleTeam.RED) {
			redPoint++;
			return;
		}

		// BLUEの場合青に1ポイント追加
		if (team == BattleTeam.BLUE) {
			bluePoint++;
			return;
		}
	}

	/**
	 * チーム分けを行うクラスを変更します
	 * @param distributor 変更するTeamDistributorを実装したクラスのコンストラクタ
	 */
	public static void setTeamDistributor(TeamDistributor distributor) {
		MatchManager.teamDistributor = distributor;
	}

	/**
	 * 各チームの初期化を行います
	 */
	private static void initializeTeams() {
		// すでに初期化されている場合はreturn
		if (initialized) {
			return;
		}

		// 赤チーム取得(なかったら作成)
		redTeam = scoreboard.getTeam("Red");
		if (redTeam == null) {
			redTeam = scoreboard.registerNewTeam("Red");
		}

		// 青チーム取得(なかったら作成)
		blueTeam = scoreboard.getTeam("Blue");
		if (blueTeam == null) {
			blueTeam = scoreboard.getTeam("Blue");
		}

		// エントリーチーム取得 (なかったら作成)
		entry = scoreboard.getTeam("Entry");
		if (entry == null) {
			entry = scoreboard.getTeam("Entry");
		}
	}
}
