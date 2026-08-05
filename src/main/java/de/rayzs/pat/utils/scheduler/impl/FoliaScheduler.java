package de.rayzs.pat.utils.scheduler.impl;

import de.rayzs.pat.utils.scheduler.PATSchedulerTask;
import io.papermc.paper.threadedregions.scheduler.*;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements PATSchedulerTask {

    private static final GlobalRegionScheduler SCHEDULER = Bukkit.getGlobalRegionScheduler();
    private static final AsyncScheduler ASYNC_SCHEDULER = Bukkit.getAsyncScheduler();

    private ScheduledTask task;

    public void getInstance(Runnable runnable, Player player) {
        if (player == null) {
            SCHEDULER.execute(BukkitLoader.getPlugin(), runnable);
        } else {
            player.getScheduler().run(BukkitLoader.getPlugin(), scheduledTask -> runnable.run(), null);
        }
    }

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable, long time, long period) {
        if (async) {
            this.task = ASYNC_SCHEDULER.runAtFixedRate(BukkitLoader.getPlugin(), __ -> runnable.run(), time, period, TimeUnit.MILLISECONDS);
            return this;
        }

        this.task = SCHEDULER.runAtFixedRate(BukkitLoader.getPlugin(), __ -> runnable.run(), time, period);
        return this;
    }

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable, long time) {
        if (async) {
            this.task = ASYNC_SCHEDULER.runDelayed(BukkitLoader.getPlugin(), __ -> runnable.run(), time, TimeUnit.MILLISECONDS);
            return this;
        }

        this.task = SCHEDULER.runDelayed(BukkitLoader.getPlugin(), __ -> runnable.run(), time);
        return this;
    }

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable) {
        if (async) {
            this.task = ASYNC_SCHEDULER.runNow(BukkitLoader.getPlugin(), __ -> runnable.run());
            return this;
        }

        this.task = SCHEDULER.run(BukkitLoader.getPlugin(), __ -> runnable.run());
        return this;
    }

    @Override
    public boolean isActive() {
        return !this.task.isCancelled();
    }

    @Override
    public void setTaskId(int taskId) {
        if(taskId == -1 || taskId == -5) {
            cancelTask();
        }
    }

    @Override
    public void cancelTask() {
        this.task.cancel();
    }
}
