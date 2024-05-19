package de.rayzs.pat.api.storage;

import de.rayzs.pat.utils.configuration.*;

public abstract class StorageTemplate {

    private final String navigatePath;
    private final ConfigurationBuilder configuration;

    public StorageTemplate(ConfigurationBuilder configuration, String navigatePath) {
        this.navigatePath = navigatePath;
        this.configuration = configuration;
        this.load();
    }

    public StorageTemplate setIfEmpty(String target, Object obj) {
        configuration.getOrSet(target, obj);
        return this;
    }

    public StorageTemplate set(String target, Object obj) {
        configuration.setAndSave(target, obj);
        return this;
    }

    public Object get(String target, Object obj) {
        configuration.getOrSet(target, obj);
        return this;
    }

    public ConfigurationBuilder getConfig() {
        return configuration;
    }
    public String getNavigatePath() { return navigatePath; }

    public abstract void save();
    public abstract void load();
}
