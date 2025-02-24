package net.azisaba.lgw.core.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;

public class MatchStartCountdownTask extends BukkitRunnable {

    private int timeLeft = 30;

    @Override
    public void run() {
        // 0の場合ゲームスタート
        if ( timeLeft <= 0 ) {
            LeonGunWar.getPlugin().getCountdown().stopCountdown();
            LeonGunWar.getPlugin().getMatchQueueManager().release();
            return;
        }

        boolean chat = false;
        boolean title = false;
        boolean sound = false;
        boolean bord = false;

        // 以下の場合残り秒数をチャット欄もしくはタイトルに表示する
        if ( timeLeft % 10 == 0 ) { // 10の倍数の場合
            chat = true;
            bord = true;
        } else if ( timeLeft <= 5 ) { // 数字が5以下の場合
            chat = true;
            title = true;
            sound = true;
            bord = true;
        }

        // chatがtrueの場合表示
        if ( chat ) {
            String msg = Chat.f("{0}&7試合開始まで残り &c{1}&7", LeonGunWar.GAME_PREFIX, SecondOfDay.f(timeLeft));
            Bukkit.broadcastMessage(msg);
        }

        // titleがtrueの場合表示
        if ( title ) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + "" + timeLeft , "", 0, 40, 10));
        }

        // soundがtrueの場合音を鳴らす
        if ( sound ) {
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1f, 1f));
        }

        if(bord){
            LeonGunWar.getPlugin().getMatchQueueManager().getQueueWorld().getPlayers().forEach(p -> {

                LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard(p,LeonGunWar.getPlugin().getScoreboardDisplayer().queueBordLines(timeLeft));

            });
        }

        // 1秒減らす
        timeLeft--;
    }
}
