package de.rayzs.pat.api.communication;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.scheduler.*;
import org.bukkit.Bukkit;

public class BackendUpdater {

    private BackendUpdater() {}


    private static PATSchedulerTask TASK;
    private static boolean CONNECTED = false,
                           WAITING = false;

    private static final long SCHEDULER_PERIOD_TIME = 20 * 10;

    public static void handle() {
        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return;
        }

        if (isRunning() || !canRun()) {
            return;
        }


        start();
    }

    public static void restart() {
        stop();
        handle();
    }

    public static void receivedKeepAlivePacket() {
        WAITING = false;
        CONNECTED = true;
    }

    public static boolean isConnected() {
        return CONNECTED;
    }

    private static void start() {
        if (isRunning()) return;

        TASK = PATScheduler.createScheduler(() -> {

            if (!canRun()) {
                stop();
                return;
            }

            if (CONNECTED) {

                if (WAITING) {
                    disconnect();

                    Communicator.Backend2Proxy.sendIdentityRequest();
                    return;
                }

                WAITING = true;

                Communicator.Backend2Proxy.sendKeepAliveRequest();
                return;
            }

            Communicator.Backend2Proxy.sendIdentityRequest();
        }, 20, SCHEDULER_PERIOD_TIME);
    }

    public static void stop() {
        disconnect();

        if (isRunning()) {
            TASK.cancelTask();
            TASK = null;
        }
    }

    private static void disconnect() {
        CONNECTED = false;
        WAITING = false;

        Logger.warning("Disconnected! Attempting to reconnect...");
    }

    private static boolean isRunning() {
        return TASK != null && TASK.isActive();
    }

    private static boolean canRun() {
        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return false;
        }

        final int onlinePlayerSize = Bukkit.getOnlinePlayers().size();
        return onlinePlayerSize >= 1;
    }
}
