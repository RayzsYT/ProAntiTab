package de.rayzs.pat.api.brand;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.api.brand.impl.BukkitServerBrand;
import de.rayzs.pat.api.brand.impl.BungeeServerBrand;
import de.rayzs.pat.api.brand.impl.VelocityServerBrand;
import de.rayzs.pat.utils.Storage;

public class CustomServerBrand {

    public static final String CHANNEL_NAME = Reflection.getMinor() < 13 ? "MC|Brand" : "minecraft:brand";
    private static final ServerBrand SERVER_BRAND = Reflection.isVelocityServer() ? new VelocityServerBrand() : Reflection.isProxyServer() ? new BungeeServerBrand() : new BukkitServerBrand();

    public static void initialize() {
        if(Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY < 1) Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY = 20;
        refreshTask();
    }
    public static void refreshTask() { SERVER_BRAND.initializeTask(); }
    public static void preparePlayer(Object playerObj) { SERVER_BRAND.preparePlayer(playerObj); }
    public static void sendBrandToPlayer(Object playerObj) { SERVER_BRAND.send(playerObj); }
}
