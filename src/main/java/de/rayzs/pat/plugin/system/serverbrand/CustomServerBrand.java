package de.rayzs.pat.plugin.system.serverbrand;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.PacketUtils;

public class CustomServerBrand {

    private static CustomServerBrand instance = null;

    public static void initialize(ServerBrand serverBrand) {

        if (instance != null) {
            Logger.warning("CustomServerBrand is already initialized!");
            return;
        }

        instance = new CustomServerBrand(serverBrand);
    }

    public static CustomServerBrand get() {
        return instance;
    }


    private final ServerBrand serverBrand;

    private CustomServerBrand(ServerBrand serverBrand) {
        this.serverBrand = serverBrand;
    }

    public void refreshTask() {
        serverBrand.initializeTask();
    }

    public void preparePlayer(Object playerObj) {
        serverBrand.preparePlayer(playerObj);
    }

    public void sendBrandToPlayer(Object playerObj) {
        serverBrand.send(playerObj);
    }

    public boolean isEnabled() {
        return Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED;
    }

    public PacketUtils.BrandManipulate createBrandPacket(Object playerObj) {
        return serverBrand.createPacket(playerObj);
    }

    public boolean isBrandTag(String tag) {
        return tag.equals("MC|Brand") || tag.equals("minecraft:brand");
    }
}
