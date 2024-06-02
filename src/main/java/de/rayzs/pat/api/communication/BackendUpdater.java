package de.rayzs.pat.api.communication;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.Bukkit;

public class BackendUpdater {

    private static int TASK = -1;

    public static void handle() {
        if(isRunning() || !shouldRun() || !Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) return;
        start();
    }

    public static boolean isRunning() {
        return TASK != -1;
    }

    public static void start() {
        if(TASK != -1) return;

        TASK = Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitLoader.getPlugin(), () -> {
            if(shouldRun()) Communicator.sendRequest();
            else stop();
        }, 5, 20);
    }

    public static void stop() {
        if(TASK == -1) return;

        Bukkit.getScheduler().cancelTask(TASK);
        TASK = -1;
    }

    private static boolean shouldRun() {
        int onlinePlayerSize = Bukkit.getOnlinePlayers().size();
        return onlinePlayerSize >= 1;
    }
}
