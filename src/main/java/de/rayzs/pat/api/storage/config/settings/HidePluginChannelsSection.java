package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;
import java.util.HashSet;

public class HidePluginChannelsSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper WHITELISTED_CHANNELS;

    private final HashSet<String> REGISTER_CHANNELS = new HashSet<>(
            Arrays.asList("register", "unregister", "minecraft:register", "minecraft:unregister")
    );

    public HidePluginChannelsSection() {
        super("plugin-channels-hider");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", false).getOrSet();
        WHITELISTED_CHANNELS = new MultipleMessagesHelper(this, "whitelisted-channels", Arrays.asList(
                "minecraft:brand",
                "bungeecord:main",
                "velocity:main"
        ));
    }

    public boolean isRegisterChannel(final String channelId) {
        return REGISTER_CHANNELS.contains(channelId);
    }
}
