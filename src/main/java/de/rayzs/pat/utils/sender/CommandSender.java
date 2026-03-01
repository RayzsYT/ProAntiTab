package de.rayzs.pat.utils.sender;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.sender.impl.BukkitSender;
import de.rayzs.pat.utils.sender.impl.BungeeSender;
import de.rayzs.pat.utils.sender.impl.VelocitySender;

import java.util.UUID;

public interface CommandSender {


    UUID CONSOLE_UUID = UUID.randomUUID();

    /**
     * Takes either the UUID, the player object, or the console sender
     * and transforms it into a PAT CommandSender.
     *
     * @param senderObj Input
     * @return CommandSender.
     */
    static CommandSender from(Object senderObj) {
        CommandSender sender;

        if (senderObj instanceof CommandSender finalSender) {
            return finalSender;
        }

        if (Reflection.isProxyServer()) {
            sender = Reflection.isVelocityServer()
                    ? new VelocitySender(senderObj)
                    : new BungeeSender(senderObj);
        } else {
            sender = new BukkitSender(senderObj);
        }

        if (sender.getUniqueId() == null) {
            return null;
        }

        return sender;
    }


    Object getSenderObject();

    boolean isConsole();
    boolean isPlayer();

    boolean isOperator();
    boolean hasPermission(String permission);

    UUID getUniqueId();

    String getName();
    String getServerName();

    void sendMessage(String message);
}
