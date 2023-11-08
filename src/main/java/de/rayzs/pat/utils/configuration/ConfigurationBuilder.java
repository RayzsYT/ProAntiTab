package de.rayzs.pat.utils.configuration;

import java.io.File;

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
    File getFile();
    boolean loadDefault();
}
