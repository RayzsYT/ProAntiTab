package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class NoPermissionSection extends ConfigStorage {

    public String MESSAGE;

    public NoPermissionSection() {
        super("no-permissions");
    }

    @Override
    public void load() {
        super.load();
        MESSAGE = new ConfigSectionHelper<String>(this, null, "&cYou are not allowed to execute this command! Missing permission: &4proantitab.%permission%").getOrSet();
    }
}
