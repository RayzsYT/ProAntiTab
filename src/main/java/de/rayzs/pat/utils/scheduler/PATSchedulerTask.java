package de.rayzs.pat.utils.scheduler;

import org.bukkit.entity.Player;

public interface PATSchedulerTask {

    void getInstance(Runnable runnable, Player player);
    PATSchedulerTask getInstance(boolean async, Runnable runnable, long time, long period);
    PATSchedulerTask getInstance(boolean async, Runnable runnable, long time);
    PATSchedulerTask getInstance(boolean async, Runnable runnable);

    boolean isActive();
    void setTaskId(int taskId);

    void cancelTask();
}
