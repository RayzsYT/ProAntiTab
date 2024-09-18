package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;

public class PermsCheckSection extends ConfigStorage {

    public String MESSAGE, PLAYER_MISSING, PLAYER_NOT_ONLINE;

    public PermsCheckSection() {
        super("perms-check");
    }

    @Override
    public void load() {
        super.load();
        MESSAGE = new ConfigSectionHelper<String>(this, "message", "&7All of &e%player%'s &7PAT-related permission: &e%permissions%").getOrSet();
        PLAYER_MISSING = new ConfigSectionHelper<String>(this, "player-is-missing", "&cPlease specify the player whose permission you want to check.").getOrSet();
        PLAYER_NOT_ONLINE = new ConfigSectionHelper<String>(this, "player-not-online", "&c%target% is not online!").getOrSet();
    }
}
