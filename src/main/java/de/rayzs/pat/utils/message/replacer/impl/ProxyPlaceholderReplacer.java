package de.rayzs.pat.utils.message.replacer.impl;

import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.Reflection;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import java.util.UUID;
import java.util.function.Consumer;

public class ProxyPlaceholderReplacer {

    private static final PlaceholderAPI PLACEHOLDER_API;

    static {
        PLACEHOLDER_API = PlaceholderAPI.createInstance();
    }

    public static boolean process(Object playerObj, String text, Consumer<String> consumer) {
        UUID uuid = null;

        if(playerObj != null)

            if(playerObj instanceof CommandSender) {
                uuid = ((CommandSender) playerObj).getUniqueId();

            } else if(Reflection.isVelocityServer()) {
                if (playerObj instanceof com.velocitypowered.api.proxy.Player)
                    uuid = ((com.velocitypowered.api.proxy.Player) playerObj).getUniqueId();

            } else if(Reflection.isProxyServer()) {
                System.out.println("Checking through proxy");
                if (playerObj instanceof net.md_5.bungee.api.connection.ProxiedPlayer) {
                    System.out.println("Is ProxiedPlayer");
                    uuid = ((net.md_5.bungee.api.connection.ProxiedPlayer) playerObj).getUniqueId();
                } else System.out.println("No proxied player ;C -> " + playerObj);
            } else {
                if(playerObj instanceof org.bukkit.entity.Player)
                    uuid = ((org.bukkit.entity.Player) playerObj).getUniqueId();
            }

        if(uuid == null) {
            System.out.println("Ignore translation: " + text);
            return false;
        }

        System.out.println("Translated: " + text);
        PLACEHOLDER_API.formatPlaceholders(text, uuid).thenAccept(consumer);

        return true;
    }
}
