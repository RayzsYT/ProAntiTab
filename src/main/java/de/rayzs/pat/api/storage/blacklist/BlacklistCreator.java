package de.rayzs.pat.api.storage.blacklist;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.*;

public class BlacklistCreator {

    public static GeneralBlacklist createGeneralBlacklist() {
        return new GeneralBlacklist("global.commands");
    }

    public static GeneralBlacklist createGeneralBlacklist(String server) {
        return new GeneralBlacklist((server != null ? "servers." + server + "." : "") + "commands");
    }

    public static GroupBlacklist createGroupBlacklist(String group) {
        return createGroupBlacklist(group, null);
    }

    public static GroupBlacklist createGroupBlacklist(String group, String server) {
        return new GroupBlacklist(group, (server != null ? ".servers." + server : ".commands"));
    }

    public static boolean exist(String server) {
        Object obj = Storage.Blacklist.getBlacklist().getConfig().get("general." + (server != null ? ".servers." + server : ".commands"));
        return obj != null;
    }

    public static boolean exist(String group, String server) {
        Object obj = Storage.Blacklist.getBlacklist().getConfig().get("groups." + group + "." + (server != null ? ".servers." + server : ".commands"));
        return obj != null;
    }
}
