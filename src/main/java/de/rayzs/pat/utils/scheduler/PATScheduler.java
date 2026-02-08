package de.rayzs.pat.utils.scheduler;

import de.rayzs.pat.utils.scheduler.impl.*;
import de.rayzs.pat.utils.Reflection;
import org.bukkit.entity.Player;

public class PATScheduler {

    public static void execute(Runnable runnable, Player player) {
        if (Reflection.isFoliaServer())
            new FoliaScheduler().getInstance(runnable, player);
        else
            new BukkitScheduler().getInstance(runnable, player);
    }

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
