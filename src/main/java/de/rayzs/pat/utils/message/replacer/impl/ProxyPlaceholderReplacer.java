package de.rayzs.pat.utils.message.replacer.impl;

import net.william278.papiproxybridge.api.PlaceholderAPI;
import java.util.function.Consumer;
import de.rayzs.pat.utils.*;
import java.util.UUID;

public class ProxyPlaceholderReplacer {

    private final PlaceholderAPI placeholderAPI;

    public ProxyPlaceholderReplacer() {
        placeholderAPI = PlaceholderAPI.createInstance();
        placeholderAPI.setRequestTimeout(1500);
    }

    public boolean process(Object playerObj, String text, Consumer<String> consumer) {
        UUID uuid = null;

        if(playerObj != null)

            if(playerObj instanceof CommandSender) {
                uuid = ((CommandSender) playerObj).getUniqueId();

            } else if(Reflection.isVelocityServer()) {
                if (playerObj instanceof com.velocitypowered.api.proxy.Player)
                    uuid = ((com.velocitypowered.api.proxy.Player) playerObj).getUniqueId();

            } else if(Reflection.isProxyServer()) {
                if (playerObj instanceof net.md_5.bungee.api.connection.ProxiedPlayer)
                    uuid = ((net.md_5.bungee.api.connection.ProxiedPlayer) playerObj).getUniqueId();

            } else {
                if(playerObj instanceof org.bukkit.entity.Player)
                    uuid = ((org.bukkit.entity.Player) playerObj).getUniqueId();
            }

        if(uuid == null) return false;
        placeholderAPI.formatPlaceholders(text, uuid).thenAccept(consumer);

        return true;
    }
}
