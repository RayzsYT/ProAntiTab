package de.rayzs.pat.plugin.system.communication;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.scheduler.*;
import org.bukkit.Bukkit;

public class BackendUpdater {

    /**
     *
     * I wasn't sure how to call this class, but this class is mainly only
     * for managing whether PAT is still connected to the proxy or not.
     * It sends a keep-alive packet and if it doesn't get a response once
     * the next iteration of the scheduler starts, it will consider itself
     * 'desynchronized' and sends an IdentityRequest packet to the proxy instead,
     * restarting the whole process until it gets into a stable connection once more.
     *
     */

    public static BackendUpdater instance;

    public static void initialize() {
        if (instance != null) {
            Logger.warning("BackendUpdater already initialized");
            return;
        }

        instance = new BackendUpdater();
    }

    public static BackendUpdater get() {
        return instance;
    }


    private BackendUpdater() {}


    private PATSchedulerTask TASK;
    private boolean CONNECTED = false,
                           WAITING = false;

    private final long SCHEDULER_PERIOD_TIME = 20 * 10;

    public void handle() {
        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return;
        }

        if (isRunning() || !canRun()) {
            return;
        }


        start();
    }

    public void restart() {
        stop();
        handle();
    }

    public void receivedKeepAlivePacket() {
        WAITING = false;
        CONNECTED = true;
    }

    public boolean isConnected() {
        return CONNECTED;
    }

    private void start() {
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

    public void stop() {
        disconnect();

        if (isRunning()) {
            TASK.cancelTask();
            TASK = null;
        }
    }

    private void disconnect() {
        CONNECTED = false;
        WAITING = false;
    }

    private boolean isRunning() {
        return TASK != null && TASK.isActive();
    }

    private boolean canRun() {
        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return false;
        }

        final int onlinePlayerSize = Bukkit.getOnlinePlayers().size();
        return onlinePlayerSize >= 1;
    }
}
