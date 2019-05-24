package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.PlayerKickMatchEvent;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.MatchMode;

public class PlayerControlListener implements Listener {

	/**
	 * 試合中のプレイヤーがサーバーから退出した場合に試合から退出させるリスナー
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		MatchManager manager = LeonGunWar.getPlugin().getManager();
		// プレイヤーが試合中でなければreturn
		if (!manager.isPlayerMatching(p)) {
			return;
		}

		// 試合から退出
		manager.kickPlayer(p);
	}

	/**
	 * LDMでリーダーが退出した際にゲームを終了させるリスナー
	 */
	@EventHandler
	public void onPlayerKicked(PlayerKickMatchEvent e) {
		Player p = e.getPlayer();
		MatchManager manager = LeonGunWar.getPlugin().getManager();

		// LDMではなかった場合return
		if (manager.getMatchMode() != MatchMode.LEADER_DEATH_MATCH) {
			return;
		}

		// 試合中のプレイヤー取得
		Map<BattleTeam, List<Player>> playerMap = manager.getTeamPlayers();

		// プレイヤーがリーダーだった場合、勝者は無しで試合を終了させる
		for (List<Player> plist : playerMap.values()) {
			if (plist.contains(p)) {
				// イベント作成、呼び出し
				MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), new ArrayList<>(),
						manager.getTeamPlayers());
				Bukkit.getPluginManager().callEvent(event);
				break;
			}
		}
	}

	/**
	 * プレイヤーが退出したときにエントリーを解除するリスナー
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQUit(PlayerQuitEvent e) {
		LeonGunWar.getPlugin().getManager().removeEntryPlayer(e.getPlayer());
	}

	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();

		// 試合中ではなかったらreturn
		if (!LeonGunWar.getPlugin().getManager().isMatching()) {
			return;
		}

		// プレイヤーが試合をしていなかったらreturn
		if (!LeonGunWar.getPlugin().getManager().getAllTeamPlayers().contains(p)) {
			return;
		}

		// Fromが試合のワールドではなかったらreturn
		if (e.getFrom() != LeonGunWar.getPlugin().getManager().getCurrentGameMap().getWorld()) {
			return;
		}

		// 退出
		LeonGunWar.getPlugin().getManager().leavePlayer(p);
	}
}
