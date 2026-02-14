package de.rayzs.pat.utils.response.action;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.MessageTranslator;

import java.util.List;
import java.util.UUID;

public interface Action {

    void executeConsoleCommand(String action, UUID uuid, String command, String message);
    void executePlayerCommand(String action, UUID uuid, String command, String message);
    void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut);
    void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier);
    void playSound(String action, UUID uuid, String soundName, float volume, float pitch);
    void sendActionbar(String action, UUID uuid, String command, String message);

    default void alert(UUID uuid, String playerName, String serverName, String command) {
        command = command.startsWith("/") ? command.substring(1) : command;
        command = StringUtils.getFirstArg(command);

        final String displayCommand = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");


        final List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", playerName,
                "%command%", displayCommand,
                "%server%", serverName);

        Communicator.Proxy2Backend.sendNotification(uuid, serverName, displayCommand);

        if (Storage.SEND_CONSOLE_NOTIFICATION)
            Logger.info(notificationMessage);

        Storage.NOTIFY_PLAYERS.stream().forEach(tmpUUID -> {
            Object p = Storage.getLoader().getPlayerObjByUUID(tmpUUID);
            if (p != null) {
                MessageTranslator.send(p, notificationMessage);
            }
        });
    }
}
