package net.azisaba.lgw.core;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.lgw.core.tasks.MatchStartCountdownTask;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MapLoader;
import net.azisaba.lgw.core.utils.Chat;

public class MatchStartCountdown {

    private final AtomicReference<BukkitTask> task = new AtomicReference<>();

    /**
     * カウントダウンが行われていない場合、カウントダウンをスタートします
     */
    public void startCountdown() {
        // すでにタイマースタートしている場合はreturn
        // Runnable取得してスタート
        task.compareAndSet(null, new MatchStartCountdownTask().runTaskTimer(LeonGunWar.getPlugin(), 0, 20));

    }

    /**
     * カウントダウンが実行されていた場合、カウントダウンを停止します
     */
    public void stopCountdown() {
        Optional.ofNullable(task.getAndSet(null)).ifPresent(BukkitTask::cancel);
    }

    /**
     * 現在カウントダウンが行われているかどうかを返します
     *
     * @return カウントダウン中かどうか
     */
    public boolean isRunning() {
        return task.get() != null;
    }
}
