package de.rayzs.pat.api.storage.blacklist;

import de.rayzs.pat.api.storage.blacklist.impl.*;
import de.rayzs.pat.api.storage.Storage;

public class BlacklistCreator {

    public static GeneralBlacklist createGeneralBlacklist() {
        return new GeneralBlacklist("commands");
    }

    public static GeneralBlacklist createGeneralBlacklist(String server) {
        server = server.toLowerCase();
        return new GeneralBlacklist("servers." + server + ".commands");
    }

    public static GroupBlacklist createGroupBlacklist(String group) {
        return new GroupBlacklist(group, "commands");
    }

    public static GroupBlacklist createGroupBlacklist(String group, String server, boolean ignoreExist) {
        server = server.toLowerCase();
        if (!exist(group, server) && !ignoreExist)
            return null;
        return new GroupBlacklist(group, "servers." + server + ".commands");
    }

    public static boolean exist(String server) {
        server = server.toLowerCase();
        Object obj = Storage.Blacklist.getBlacklist().getConfig().get("global.servers." + server);
        return obj != null;
    }

    public static boolean exist(String group, String server) {
        server = server.toLowerCase();
        Object obj = Storage.Blacklist.getBlacklist().getConfig().get("groups." + group + ".servers." + server);
        return obj != null;
    }
}
