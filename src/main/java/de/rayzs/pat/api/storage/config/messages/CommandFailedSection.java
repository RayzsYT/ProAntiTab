package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class CommandFailedSection extends ConfigStorage {

    public String MESSAGE;

    public CommandFailedSection() {
        super("command-failed");
    }

    @Override
    public void load() {
        super.load();
        MESSAGE = new ConfigSectionHelper<String>(this, null, "&cFailed to execute this command! Use \"/pat\" to see all available commands.").getOrSet();
    }
}
