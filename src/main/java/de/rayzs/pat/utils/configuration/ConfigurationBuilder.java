package de.rayzs.pat.utils.configuration;

import java.io.File;
import java.util.Collection;

public interface ConfigurationBuilder {
    void reload();
    void save();
    String getFilePath();
    ConfigurationBuilder set(String path, String target, Object object);
    ConfigurationBuilder set(String target, Object object);
    ConfigurationBuilder setAndSave(String path, String target, Object object);
    ConfigurationBuilder setAndSave(String target, Object object);
    Object getOrSet(String path, String target, Object object);
    Object getOrSet(String target, Object object);
    Object get(String target);
    Object get(String path, String target);
    Collection<String> getKeys(boolean deep);
    Collection<String> getKeys(String section, boolean deep);
    File getFile();
    boolean loadDefault();
}
