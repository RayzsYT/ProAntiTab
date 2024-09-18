package de.rayzs.pat.utils.scheduler;

import de.rayzs.pat.utils.scheduler.impl.*;
import de.rayzs.pat.utils.Reflection;

public class PATScheduler {

    public static PATSchedulerTask createScheduler(Runnable runnable, int time, int period) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(runnable, time, period)
                : new BukkitScheduler().getInstance(runnable, time, period);
    }

    public static PATSchedulerTask createScheduler(Runnable runnable, int time) {
        return Reflection.isFoliaServer()
                ? new FoliaScheduler().getInstance(runnable, time)
                : new BukkitScheduler().getInstance(runnable, time);
    }
}
