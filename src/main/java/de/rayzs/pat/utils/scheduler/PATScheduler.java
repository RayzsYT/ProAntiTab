package de.rayzs.pat.utils.scheduler;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.scheduler.impl.BukkitScheduler;
import de.rayzs.pat.utils.scheduler.impl.FoliaScheduler;

public class PATScheduler {

    public static PATSchedulerTask createScheduler(Runnable runnable, long time, long period) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(false, runnable, time, period)
                : new BukkitScheduler().getInstance(false, runnable, time, period);
    }

    public static PATSchedulerTask createScheduler(Runnable runnable, long time) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(false, runnable, time)
                : new BukkitScheduler().getInstance(false, runnable, time);
    }

    public static PATSchedulerTask createScheduler(Runnable runnable) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(false, runnable)
                : new BukkitScheduler().getInstance(false, runnable);
    }

    public static PATSchedulerTask createAsyncScheduler(Runnable runnable, long time, long period) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(true, runnable, time, period)
                : new BukkitScheduler().getInstance(true, runnable, time, period);
    }

    public static PATSchedulerTask createAsyncScheduler(Runnable runnable, long time) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(true, runnable, time)
                : new BukkitScheduler().getInstance(true, runnable, time);
    }

    public static PATSchedulerTask createAsyncScheduler(Runnable runnable) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(true, runnable)
                : new BukkitScheduler().getInstance(true, runnable);
    }
}
