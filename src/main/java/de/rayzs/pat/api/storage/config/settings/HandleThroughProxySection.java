package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;

public class HandleThroughProxySection extends ConfigStorage {

    public boolean ENABLED;

    public boolean LOAD_FROM_ENV;
    public String ENV_NAME;

    public String TOKEN;

    public HandleThroughProxySection() {
        super("handle-through-proxy");
    }

    @Override
    public void load() {
        super.load();
        if(Reflection.isProxyServer()) return;

        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", false).getOrSet();
        TOKEN = new ConfigSectionHelper<String>(this, "token", "insert-token-of-proxy-here").getOrSet();

        LOAD_FROM_ENV = new ConfigSectionHelper<Boolean>(this, "load-from-env.enabled", false).getOrSet();
        ENV_NAME = new ConfigSectionHelper<String>(this, "load-from-env.name", false).getOrSet();
    }
}
