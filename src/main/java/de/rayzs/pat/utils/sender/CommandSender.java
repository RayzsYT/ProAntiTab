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

        if (senderObj instanceof CommandSender self) {
            return self;
        }

        if (Reflection.isProxyServer()) {
            sender = Reflection.isVelocityServer()
                    ? VelocitySender.from(senderObj)
                    : BungeeSender.from(senderObj);
        } else {
            sender = BukkitSender.from(senderObj);
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
