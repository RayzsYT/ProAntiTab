package de.rayzs.pat.plugin.system.communication.cph.impl;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.system.communication.cph.CommunicationPacketHandler;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitCommunicationHandler implements CommunicationPacketHandler {

    @Override
    public void handleReceivedPacket(CommunicationPackets.PATPacket packet) {

        if (packet instanceof CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket) {
            handleDataSyncPacket(dataSyncPacket);
            return;
        }

        if (packet instanceof CommunicationPackets.Proxy2Backend.NotificationPacket notificationPacket) {
            handleNotificationPacket(notificationPacket);
            return;
        }

        if (packet instanceof CommunicationPackets.Proxy2Backend.UpdatePacket updatePacket) {
            handleUpdateCommandsPacket(updatePacket);
            return;
        }

        if (packet instanceof CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket executePlayerCommandPacket) {
            handlePlayerExecuteCommandPacket(executePlayerCommandPacket);
            return;
        }

        if (packet instanceof CommunicationPackets.Proxy2Backend.ConsoleMessagePacket consoleMessagePacket) {
            handleConsoleMessagePacket(consoleMessagePacket);
            return;
        }

        if (packet instanceof CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket executeConsoleCommandPacket) {
            handleConsoleExecuteCommandPacket(executeConsoleCommandPacket);
        }
    }

    private void handleNotificationPacket(CommunicationPackets.Proxy2Backend.NotificationPacket packet) {
        if (!Storage.SEND_CONSOLE_NOTIFICATION) return;

        final Player player = Bukkit.getPlayer(packet.playerId());
        if (player == null) {
            return;
        }

        final List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getName(),
                "%command%", packet.command(),
                "%server%", Storage.SERVER_NAME,
                "%world%", player.getWorld().getName());

        Logger.info(notificationMessage);
    }

    private void handleUpdateCommandsPacket(CommunicationPackets.Proxy2Backend.UpdatePacket packet) {
        if (Reflection.isBefore(1, 13)) {
            return;
        }

        PATScheduler.createScheduler(() -> {
            if (packet.forEveryone()) {
                Storage.getLoader().updateCommands();
                return;
            }

            final CommandSender sender = CommandSender.from(packet.playerId());
            if (sender != null) {
                Storage.getLoader().updateCommands(sender);
            }

        });
    }

    private void handlePlayerExecuteCommandPacket(CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket packet) {
        if (isP2BDisabled()) return;

        final Player player = Bukkit.getPlayer(packet.playerId());
        if (player == null) return;

        PATScheduler.execute(() -> Bukkit.dispatchCommand(player, packet.command()), player);
    }

    private void handleConsoleExecuteCommandPacket(CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket packet) {
        if (isP2BDisabled()) return;

        PATScheduler.execute(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), packet.command()), null);
    }

    private void handleConsoleMessagePacket(CommunicationPackets.Proxy2Backend.ConsoleMessagePacket packet) {
        if (isP2BDisabled()) return;

        Logger.info(packet.message());
    }

    private void handleDataSyncPacket(CommunicationPackets.Proxy2Backend.DataSyncPacket packet) {
        Storage.ConfigSections.Messages.PREFIX.PREFIX = packet.messages().prefix();
        Storage.ConfigSections.Settings.AUTO_LOWERCASE_COMMANDS.ENABLED = packet.autoLowerCase().enabled();
        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED = packet.unknownCommand().enabled();
        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE = packet.unknownCommand().message();

        PATEventHandler.callReceiveSyncEvents(packet);
    }

    private boolean isP2BDisabled() {
        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ALLOW_P2B_ACTIONS) {
            Logger.warning("P2B actions are currently disabled! You need to enable them manually.");
            Logger.warning("For that, go to your 'plugins/ProAntiTab/config.yml' file and enable 'handle-through-proxy -> allow-p2b-actions'.");
            return true;
        }

        return false;
    }
}
