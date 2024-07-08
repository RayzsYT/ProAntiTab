package de.rayzs.pat.utils.adapter;

import de.rayzs.pat.plugin.logger.Logger;
import com.viaversion.viaversion.api.*;
import java.util.UUID;

public class ViaVersionAdapter {

    private static ViaAPI API;

    public static void initialize() {
        Logger.info("Successfully hooked into ViaVersion for easier usage.");

        API = Via.getAPI();
    }

    public static int getPlayerProtocol(UUID uuid) {
        return API.getPlayerVersion(uuid);
    }
}
