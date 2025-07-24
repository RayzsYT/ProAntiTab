package de.rayzs.pat.api.brand;

import de.rayzs.pat.api.brand.impl.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Reflection;

public class CustomServerBrand {

    public static final String CHANNEL_NAME = Reflection.getMinor() < 13 ? "MC|Brand" : "minecraft:brand";
    private static final ServerBrand SERVER_BRAND = Reflection.isVelocityServer() ? new VelocityServerBrand() : Reflection.isProxyServer() ? new BungeeServerBrand() : new BukkitServerBrand();

    public static void initialize() { refreshTask(); }
    public static void refreshTask() { SERVER_BRAND.initializeTask(); }
    public static void preparePlayer(Object playerObj) { SERVER_BRAND.preparePlayer(playerObj); }
    public static void sendBrandToPlayer(Object playerObj) { SERVER_BRAND.send(playerObj); }

    public static PacketUtils.BrandManipulate createBrandPacket(Object playerObj) { return SERVER_BRAND.createPacket(playerObj); }
    public static boolean isEnabled() { return Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED; }

    public static boolean isBrandTag(String tag) {
        return tag.equals("MC|Brand") || tag.equals("minecraft:brand");
    }
}
