package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;

public class OnlyForProxySection extends ConfigStorage {

    public String MESSAGE;

    public OnlyForProxySection() {
        super("only-for-proxy");
    }

    @Override
    public void load() {
        super.load();
        if(Reflection.isProxyServer()) return;
        MESSAGE = new ConfigSectionHelper<String>(this, null, "&cThis command works on Bungeecord/Velocity servers only!").getOrSet();
    }
}
