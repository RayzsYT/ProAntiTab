package de.rayzs.pat.utils.adapter;

import de.rayzs.pat.api.storage.Storage;
import com.viaversion.viaversion.api.*;
import java.util.UUID;

public class ViaVersionAdapter {

    private static ViaAPI API;

    public static void initialize() {
        Storage.USE_VIAVERSION = true;
        API = Via.getAPI();
    }

    public static int getPlayerProtocol(UUID uuid) {
        return API.getPlayerVersion(uuid);
    }
}
