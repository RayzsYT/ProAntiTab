package de.rayzs.pat.api.storage.blacklist;

import de.rayzs.pat.api.storage.blacklist.impl.*;

public class BlacklistCreator {

    public static GeneralBlacklist createGeneralBlacklist() {
        return createGeneralBlacklist(null);
    }

    public static GeneralBlacklist createGeneralBlacklist(String server) {
        return new GeneralBlacklist((server != null ? "servers." + server : "commands"));
    }

    public static GroupBlacklist createGroupBlacklist(String group) {
        return createGroupBlacklist(group, null);
    }

    public static GroupBlacklist createGroupBlacklist(String group, String server) {
        return new GroupBlacklist(group, (server != null ? ".servers." + server : ".commands"));
    }
}
