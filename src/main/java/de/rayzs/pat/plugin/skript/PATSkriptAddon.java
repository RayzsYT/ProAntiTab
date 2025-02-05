package de.rayzs.pat.plugin.skript;

import ch.njol.skript.*;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.api.event.events.bukkit.ExecuteCommandEvent;
import de.rayzs.pat.plugin.skript.impl.events.ExecuteCommandSkriptEvent;

public class PATSkriptAddon {

    public static SkriptAddon ADDON;

    public static void initialize(JavaPlugin javaPlugin) {
        ADDON = Skript.getAddon(javaPlugin);

        Skript.registerEvent("PAT Command Execution", ExecuteCommandSkriptEvent.class, ExecuteCommandEvent.class, "if [command] is (blocked|cancelled|not blocked|not cancelled)");
    }


}
