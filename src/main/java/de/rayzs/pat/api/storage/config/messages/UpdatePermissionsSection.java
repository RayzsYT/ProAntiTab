package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;

public class UpdatePermissionsSection extends ConfigStorage {

    public String UPDATED;

    public UpdatePermissionsSection() {
        super("update-permissions");
    }

    @Override
    public void load() {
        super.load();
        UPDATED = new ConfigSectionHelper<String>(this, null, "&aUpdated permissions!").getOrSet();
    }
}
