package de.rayzs.pat.plugin.converter;

import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.Configurator;

import java.io.File;
import java.util.List;

public abstract class Converter {

    private final String pluginName, fileName, folderPath;
    protected final ConfigurationBuilder config;

    public Converter(String pluginName, String folderName, String fileName) {
        this.pluginName = pluginName;
        this.fileName = fileName;
        this.folderPath = "./plugins/" + folderName;
        this.config = exists() ? Configurator.get(fileName, folderPath) : null;
    }

    public String getPluginName() {
        return pluginName;
    }

    public boolean exists() {
        return new File(folderPath, fileName + ".yml").exists();
    }

    protected void applyStorage(BlacklistStorage storage, List<String> commands) {
        for (String command : commands) {
            boolean exist = command.contains(" ")
                    ? storage.getCommands().contains(command)
                    : storage.isListed(command);

            if (exist) continue;

            storage.add(command);
        }

        storage.save();
    }

    public abstract void apply();
}
