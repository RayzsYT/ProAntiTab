package de.rayzs.pat.utils.scheduler;

public interface PATSchedulerTask {

    PATSchedulerTask getInstance(boolean async, Runnable runnable, int time, int period);
    PATSchedulerTask getInstance(boolean async, Runnable runnable, int time);

    boolean isActive();
    void setTaskId(int taskId);

    void cancelTask();
}
