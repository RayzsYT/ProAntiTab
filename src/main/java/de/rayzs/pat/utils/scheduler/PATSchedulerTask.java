package de.rayzs.pat.utils.scheduler;

public interface PATSchedulerTask {

    PATSchedulerTask getInstance(boolean async, Runnable runnable, long time, long period);

    PATSchedulerTask getInstance(boolean async, Runnable runnable, long time);

    PATSchedulerTask getInstance(boolean async, Runnable runnable);

    boolean isActive();

    void setTaskId(int taskId);

    void cancelTask();
}
