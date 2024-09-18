package de.rayzs.pat.utils.scheduler.impl;

import de.rayzs.pat.utils.scheduler.PATSchedulerTask;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.Bukkit;

public class BukkitScheduler implements PATSchedulerTask {

    private int taskId = -5;

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable, int time, int period) {
        this.taskId = async
                ? Bukkit.getScheduler().scheduleAsyncRepeatingTask(BukkitLoader.getPlugin(), runnable, time, period)
                : Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitLoader.getPlugin(), runnable, time, period);
        return this;
    }

    @Override
    public PATSchedulerTask getInstance(boolean async, Runnable runnable, int time) {
        this.taskId = async
                ? Bukkit.getScheduler().scheduleAsyncDelayedTask(BukkitLoader.getPlugin(), runnable, time)
                : Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitLoader.getPlugin(), runnable, time);
        return this;
    }

    @Override
    public boolean isActive() {
        return this.taskId != 1;
    }

    @Override
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public void cancelTask() {
        Bukkit.getScheduler().cancelTask(taskId);
        this.taskId = -5;
    }
}
