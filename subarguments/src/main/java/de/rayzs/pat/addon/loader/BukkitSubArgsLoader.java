package de.rayzs.pat.addon.loader;

import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.addon.SubArgsAddon;

public class BukkitSubArgsLoader extends JavaPlugin {

    @Override
    public void onEnable() {
        SubArgsAddon.onLoad();
    }
}
