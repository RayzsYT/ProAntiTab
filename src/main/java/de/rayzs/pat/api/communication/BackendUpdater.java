package de.rayzs.pat.api.communication;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.scheduler.*;
import org.bukkit.Bukkit;

public class BackendUpdater {

    private static PATSchedulerTask TASK;

    public static void handle() {
        if(isRunning() || !shouldRun() || !Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) return;
        start();
    }

    public static boolean isRunning() {
        return TASK.isActive();
    }

    public static void start() {
        if(TASK != null && TASK.isActive()) return;

        TASK = PATScheduler.createScheduler(() -> {
            if (shouldRun()) Communicator.sendRequest();
            else stop();
        }, 5, 20);
    }

    public static void stop() {
        if(!TASK.isActive()) return;

        TASK.cancelTask();
    }

    private static boolean shouldRun() {
        int onlinePlayerSize = Bukkit.getOnlinePlayers().size();
        return onlinePlayerSize >= 1;
    }
}
