package de.rayzs.pat.utils.sender;

import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.sender.impl.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandSenderHandler {

    public static final UUID CONSOLE_UUID = UUID.randomUUID();

    private static final ExpireCache<UUID, CommandSender> CACHE = new ExpireCache<>(1, TimeUnit.HOURS);


    public static CommandSender from(Object senderObj) {
        UUID uuid = extractUUID(senderObj);

        if (CACHE.contains(uuid)) {
            return CACHE.get(uuid);
        }

        CommandSender sender;

        if (Reflection.isProxyServer()) {
            sender = Reflection.isVelocityServer()
                    ? new VelocitySender(senderObj)
                    : new BungeeSender(senderObj);
        } else {
            sender = new BukkitSender(senderObj);
        }

        if (sender.getUniqueId() == null)
            return null;

        return CACHE.putAndGet(uuid, sender);
    }

    private static UUID extractUUID(Object targetObj) {

        if (Reflection.isProxyServer()) {

            if (Reflection.isVelocityServer()) {
                return targetObj instanceof com.velocitypowered.api.proxy.Player
                        ? ((com.velocitypowered.api.proxy.Player) targetObj).getUniqueId()
                        : CONSOLE_UUID;
            }

            return targetObj instanceof net.md_5.bungee.api.connection.ProxiedPlayer
                    ? ((net.md_5.bungee.api.connection.ProxiedPlayer) targetObj).getUniqueId()
                    : CONSOLE_UUID;

        }

        if (targetObj instanceof org.bukkit.entity.Player) {
            return ((org.bukkit.entity.Player) targetObj).getUniqueId();
        }

        return CONSOLE_UUID;
    }
}