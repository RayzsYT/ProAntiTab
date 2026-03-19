package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class InjectionFailedSection extends ConfigStorage {

    public boolean ENABLED, SUPPRESS_EXCEPTIONS;
    public ConfigSectionHelper<String> KICK_MESSAGE, CONSOLE_MESSAGE;

    public InjectionFailedSection() {
        super("injection-failed");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "kick-on-failure", true).getOrSet();
        SUPPRESS_EXCEPTIONS = new ConfigSectionHelper<Boolean>(this, "suppress-exceptions", false).getOrSet();
        KICK_MESSAGE = new ConfigSectionHelper<>(this, "kick-message", "&cInjection process failed! Please reconnect.");
        CONSOLE_MESSAGE = new ConfigSectionHelper<>(this, "console-message", "&cInjection process failed! (%player%)");
    }
}