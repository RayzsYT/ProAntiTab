package de.rayzs.pat.api.brand;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.api.brand.impl.*;

public class CustomServerBrand {

    public static final String CHANNEL_NAME = Reflection.getMinor() < 13 ? "MC|Brand" : "minecraft:brand";
    private static final ServerBrand SERVER_BRAND = Reflection.isVelocityServer() ? new VelocityServerBrand() : Reflection.isProxyServer() ? new BungeeServerBrand() : new BukkitServerBrand();

    public static void initialize() { refreshTask(); }
    public static void refreshTask() { SERVER_BRAND.initializeTask(); }
    public static void preparePlayer(Object playerObj) { SERVER_BRAND.preparePlayer(playerObj); }
    public static void sendBrandToPlayer(Object playerObj) { SERVER_BRAND.send(playerObj); }
}
