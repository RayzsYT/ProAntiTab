package de.rayzs.pat.utils.scheduler;

public interface PATSchedulerTask {

    PATSchedulerTask getInstance(Runnable runnable, int time, int period);
    PATSchedulerTask getInstance(Runnable runnable, int time);

    boolean isActive();
    void setTaskId(int taskId);

    void cancelTask();
}
