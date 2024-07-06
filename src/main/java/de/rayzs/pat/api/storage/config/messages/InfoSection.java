package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;
import java.util.Arrays;

public class InfoSection extends ConfigStorage {

    public String VERSION_OUTDATED, VERSION_UPDATED, SYNC_TIME, SYNC_DISABLED;
    public MultipleMessagesHelper MESSAGE;

    public InfoSection() {
        super("info");
    }

    @Override
    public void load() {
        super.load();

        VERSION_UPDATED = new ConfigSectionHelper<String>(this, "version.updated", "&aLatest version").getOrSet();
        VERSION_OUTDATED = new ConfigSectionHelper<String>(this, "version.outdated", "&cOutdated (%newest_version%)").getOrSet();
        SYNC_TIME = new ConfigSectionHelper<String>(this, "proxy-sync.time", "&e%time%").getOrSet();
        SYNC_DISABLED = new ConfigSectionHelper<String>(this, "proxy-sync.disabled", "&cDisabled").getOrSet();

        MESSAGE = new MultipleMessagesHelper(this, "message",
                Arrays.asList(
                        "&7ProAntiTab information:"
                        , "&7  Version: &e%current_version%"
                        , "&7  Status: %updated%"
                        , "&7  Last sync with proxy: &e%sync%"
                )
        );
    }
}
