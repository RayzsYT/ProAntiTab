package de.rayzs.pat.utils.configuration.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.configuration.yaml.Configuration;
import de.rayzs.pat.utils.configuration.yaml.ConfigurationProvider;
import de.rayzs.pat.utils.configuration.yaml.YamlConfiguration;

import java.io.File;
import java.util.Collection;

public class ProxyConfigurationBuilder implements ConfigurationBuilder {

    private String fileName, filePath;
    private File file;
    private boolean loadDefault;
    private Configuration configuration;

    public ProxyConfigurationBuilder(String fileName) {
        init(fileName);
    }

    public ProxyConfigurationBuilder(String filePath, String fileName) {
        this.filePath = filePath;
        init(fileName);
    }

    protected void init(String fileName) {
        filePath = filePath == null ? "./plugins/ProAntiTab" : filePath;
        File directory = new File(filePath);

        this.fileName = fileName;

        try {
            if (!directory.isDirectory()) directory.mkdirs();
            file = new File(filePath, fileName + ".yml");
            loadDefault = !file.exists();
            if (!file.exists()) file.createNewFile();
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public void reload() {
        init(this.fileName);
    }

    @Override
    public void save() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
        } catch (Exception exception) {
            Logger.warning("Could not save configuration file! [file=" + fileName + ", message=" + exception.getMessage() + "]");
        }
    }

    @Override
    public ConfigurationBuilder set(String path, String target, Object object) {
        this.configuration.set(((path != null) ? (path + ".") : "") + target, object instanceof String ? ((String) object).replace("§", "&") : object);
        return this;
    }

    @Override
    public ConfigurationBuilder set(String target, Object object) {
        set(null, target, object);
        return this;
    }

    @Override
    public ConfigurationBuilder setAndSave(String path, String target, Object object) {
        set(path, target, object);
        save();
        return this;
    }

    @Override
    public ConfigurationBuilder setAndSave(String target, Object object) {
        set(target, object);
        save();
        return this;
    }

    @Override
    public Object getOrSet(String path, String target, Object object) {
        Object result = get(path, target);
        if (result != null)
            return result;

        if (fileName.equals("config")) {
            if (ConfigUpdater.canUpdate()) {
                String section = ConfigUpdater.getSection(path + "." + target, true);
                if (configuration.getSection(section).getKeys().size() == 0) {
                    ConfigUpdater.updateConfigFile(this, path + "." + target, true);
                    Logger.warning("Section '" + section + "' is missing! Loading default section from online config.yml.");

                    reload();
                    return get(path, target);
                }
            }
        }

        if (fileName.equals("config")) {
            ConfigUpdater.addMissingPart(path + "." + target);
            return object;
        }

        set(path, target, object);
        save();

        return get(path, target);
    }

    @Override
    public Object getOrSet(String target, Object object) {
        Object result = get(target);
        if (result != null)
            return result;

        if (fileName.equals("config")) {
            if (ConfigUpdater.canUpdate()) {
                String section = ConfigUpdater.getSection(target, true);
                if (configuration.getSection(section).getKeys().size() == 0) {
                    ConfigUpdater.updateConfigFile(this, target, true);
                    Logger.warning("Section '" + section + "' is missing! Loading default section from online config.yml.");

                    reload();
                    return get(target);
                }
            }
        }

        if (fileName.equals("config")) {
            ConfigUpdater.addMissingPart(target);
            return object;
        }

        set(target, object);
        save();

        return get(target);
    }

    @Override
    public Object get(String target) {
        return get(null, target);
    }

    @Override
    public Object get(String path, String target) {
        Object object = this.configuration.get(((path != null) ? (path + ".") : "") + target);
        if (object instanceof String) {
            String objString = (String) object;
            return objString.replace("&", "§");
        }
        return object;
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        return this.configuration.getKeys();
    }

    @Override
    public Collection<String> getKeys(String section, boolean deep) {
        return this.configuration.getSection(section).getKeys();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean loadDefault() {
        return loadDefault;
    }
}
