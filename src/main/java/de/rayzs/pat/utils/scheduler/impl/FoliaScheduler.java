package de.rayzs.pat.utils.scheduler.impl;

import de.rayzs.pat.utils.scheduler.PATSchedulerTask;
import io.papermc.paper.threadedregions.scheduler.*;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.Bukkit;

public class FoliaScheduler implements PATSchedulerTask {

    private static final GlobalRegionScheduler SCHEDULER = Bukkit.getGlobalRegionScheduler();

    private ScheduledTask task;

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable, long time, long period) {
        this.task = SCHEDULER.runAtFixedRate(BukkitLoader.getPlugin(), __ -> runnable.run(), time, period);
        return this;
    }

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable, long time) {
        this.task = SCHEDULER.runDelayed(BukkitLoader.getPlugin(), __ -> runnable.run(), time);
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
