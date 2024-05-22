package de.rayzs.pat.api.storage.blacklist;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.*;
import de.rayzs.pat.utils.Reflection;

public class BlacklistCreator {

    public static GeneralBlacklist createGeneralBlacklist() {
        return new GeneralBlacklist((Reflection.isProxyServer() ? "global." : "") + "commands");
    }

    public static GeneralBlacklist createGeneralBlacklist(String server) {
        //return new GeneralBlacklist((server != null ? "servers." + server + "." : "") + "commands");
        return new GeneralBlacklist("global.servers." + server);
    }

    public static GroupBlacklist createGroupBlacklist(String group) {
        return new GroupBlacklist(group, ".commands");
    }

    public static GroupBlacklist createGroupBlacklist(String group, String server) {
        //return new GroupBlacklist(group, (server != null ? ".servers." + server : "") + ".commands");
        return new GroupBlacklist(group, ".servers." + server);
    }

    public static boolean exist(String server) {
        //Object obj = Storage.Blacklist.getBlacklist().getConfig().get("general." + (server != null ? "servers." + server + "." : "") + "commands");
        Object obj = Storage.Blacklist.getBlacklist().getConfig().get("general.servers." + server);
        return obj != null;
    }

    public static boolean exist(String group, String server) {
        //Object obj = Storage.Blacklist.getBlacklist().getConfig().get("groups." + group + "." + (server != null ? "servers." + server + "." : "") + "commands");
        Object obj = Storage.Blacklist.getBlacklist().getConfig().get("groups." + group + ".servers." + server);
        return obj != null;
    }
}
