package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;

public class UpdateSection extends ConfigStorage {

    public boolean ENABLED;
    public int PERIOD;
    public MultipleMessagesHelper NOTIFICATION;

    public UpdateSection() {
        super("updater");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        PERIOD = new ConfigSectionHelper<Integer>(this, "period", 10000).getOrSet();
        NOTIFICATION = new MultipleMessagesHelper(this, "notification", Arrays.asList(
                "&8[&4ProAntiTab&8] &cThere is a new version available! (%newest_version%)",
                "&8[&4ProAntiTab&8] &cYou are still using the %current_version%.",
                "&8[&4ProAntiTab&8] &cGet the newest version here:",
                "&8[&4ProAntiTab&8] &ehttps://www.rayzs.de/products/proantitab/page"));
    }
}
