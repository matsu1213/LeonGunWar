package net.azisaba.lgw.core.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;

public class MatchStartCountdownTask extends BukkitRunnable {

	//			private int timeLeft = 21;
	private int timeLeft = 5; // デバッグ

	@Override
	public void run() {
		// 1秒減らす
		timeLeft--;

		// 0の場合ゲームスタート
		if (timeLeft <= 0) {
			LeonGunWar.getPlugin().getCountdown().stopCountdown();
			LeonGunWar.getPlugin().getManager().startMatch();
			return;
		}

		boolean chat = false;
		boolean title = false;

		// 以下の場合残り秒数をチャット欄もしくはタイトルに表示する
		if (timeLeft % 10 == 0) { // 10の倍数の場合
			chat = true;
		} else if (timeLeft <= 5) { // 数字が5以下の場合
			chat = true;
			title = true;
		}

		// chatがtrueの場合表示
		if (chat) {
			String msg = LeonGunWar.GAME_PREFIX + ChatColor.GRAY + "試合開始まで残り " + ChatColor.RED + timeLeft + "秒"
					+ ChatColor.GRAY + "！";
			Bukkit.broadcastMessage(msg);
		}

		// titleがtrueの場合表示
		if (title) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.sendTitle(timeLeft + "", "", 0, 40, 10);
			}
		}
	}
}
