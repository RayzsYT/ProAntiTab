package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.permission.PermissionUtil;

public class AutoLowercaseCommandsSection extends ConfigStorage {

    public boolean ENABLED;

    public AutoLowercaseCommandsSection() {
        super("auto-lowercase-commands");
    }

    @Override
    public void load() {
        super.load();

        if (Reflection.isProxyServer())
            return;

        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
    }

    public boolean isCommand(String command) {
        if(!ENABLED) return false;

        return !StringUtils.isLowercased(command);
    }

    public boolean doesBypass(Object targetObj) {
        return !ENABLED || PermissionUtil.hasPermission(targetObj, "auto-lowercase");
    }
}
