package de.rayzs.pat.utils.sender;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.sender.impl.*;
import java.util.UUID;

public class CommandSenderHandler {

    public static final UUID CONSOLE_UUID = UUID.randomUUID();

    public static CommandSender from(Object senderObj) {
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
}