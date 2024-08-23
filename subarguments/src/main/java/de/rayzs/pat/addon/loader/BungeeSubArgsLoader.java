package de.rayzs.pat.addon.loader;

import de.rayzs.pat.utils.configuration.impl.ProxyConfigurationBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import de.rayzs.pat.addon.SubArgsAddon;

public class BungeeSubArgsLoader extends Plugin {

    @Override
    public void onEnable() {
        SubArgsAddon.onLoad(new ProxyConfigurationBuilder("subarguments", "./plugins/ProAntiTab/addons"));
    }
}
