package de.rayzs.pat.utils.configuration.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import java.io.File;
import java.util.Collection;

public class BukkitConfigurationBuilder implements ConfigurationBuilder {

    private String fileName, filePath;
    private File file;
    private boolean loadDefault;
    private YamlConfiguration configuration;

    public BukkitConfigurationBuilder(String fileName) {
        init(fileName);
    }

    public BukkitConfigurationBuilder(String fileName, String filePath) {
        this.filePath = filePath;
        init(fileName);
    }

    protected void init(String fileName) {
        this.fileName = fileName;
        this.file = new File((this.filePath == null) ? "./plugins/ProAntiTab" : this.filePath, fileName + ".yml");
        this.loadDefault = !this.file.exists();
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
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
        try { this.configuration.save(this.file);
        } catch (Exception exception) {
            Logger.info("Could not save configuration file! [file=" + this.fileName + ", message=" + exception.getMessage() + "]");
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
        set(path, target, object);
        save();
        return get(path, target);
    }

    @Override
    public Object getOrSet(String target, Object object) {
        Object result = get(target);
        if (result != null)
            return result;
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
            return ChatColor.translateAlternateColorCodes('&', objString);
        }
        return object;
    }

    @Override
    public Collection<String> getKeys(boolean deep) {
        return this.configuration.getKeys(deep);
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
