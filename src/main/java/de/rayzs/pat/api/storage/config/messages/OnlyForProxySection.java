package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class OnlyForProxySection extends ConfigStorage {

    public String MESSAGE;

    public OnlyForProxySection() {
        super("only-for-proxy");
    }

    @Override
    public void load() {
        super.load();
        MESSAGE = new ConfigSectionHelper<String>(this, "", "&cThis command works on Bungeecord/Velocity servers only!").getOrSet();
    }
}
