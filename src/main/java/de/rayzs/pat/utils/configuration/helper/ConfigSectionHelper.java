package de.rayzs.pat.utils.configuration.helper;

import de.rayzs.pat.api.storage.StorageTemplate;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;

public class ConfigSectionHelper<T> {

    private final ConfigurationBuilder configuration;
    private final String path;
    private final Object obj;

    public ConfigSectionHelper(StorageTemplate config, String path, Object obj) {
        this.configuration = config.getConfig();
        this.path = config.getNavigatePath() + (path != null ? "." + path : "");
        ;
        this.obj = obj;
    }

    public boolean exist() {
        return get() != null;
    }

    public void set(Object obj) {
        configuration.setAndSave(path, obj);
    }

    public T get() {
        return (T) configuration.get(path);
    }

    public T getOrSet() {
        return (T) configuration.getOrSet(path, obj);
    }
}
