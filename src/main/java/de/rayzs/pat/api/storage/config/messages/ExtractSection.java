package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

public class ExtractSection extends ConfigStorage {

    public MultipleMessagesHelper MESSAGE;

    public ExtractSection() {
        super("extract");
    }

    public String USAGE, PLUGIN_NOT_FOUND, SUCCESS;

    @Override
    public void load() {
        super.load();

        if (Reflection.isProxyServer()) {
            return;
        }

        USAGE = new ConfigSectionHelper<String>(this, "usage", "&cUsage: /pat extract [plugin] <group/use-colon> : <use-colon>").getOrSet();
        PLUGIN_NOT_FOUND = new ConfigSectionHelper<String>(this, "plugin-not-found", "&cPlugin %plugin% not found!").getOrSet();
        SUCCESS = new ConfigSectionHelper<String>(this, "success", "&aSuccessfully extracted &e%amount% &anew commands!").getOrSet();

    }
}
