package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;

public class UpdatePermissionsSection extends ConfigStorage {

    public String UPDATE_ALL, UPDATE_SPECIFIC, PLAYER_NOT_ONLINE;

    public UpdatePermissionsSection() {
        super("update-permissions");
    }

    @Override
    public void load() {
        super.load();
        UPDATE_ALL = new ConfigSectionHelper<String>(this, "all-players", "&aUpdated permissions!").getOrSet();
        UPDATE_SPECIFIC = new ConfigSectionHelper<String>(this, "specific-player", "&aUpdated %target%'s permissions!").getOrSet();
        PLAYER_NOT_ONLINE = new ConfigSectionHelper<String>(this, "player-not-online", "&c%target% is not online!").getOrSet();
    }
}
