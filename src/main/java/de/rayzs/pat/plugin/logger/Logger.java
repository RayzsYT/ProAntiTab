package de.rayzs.pat.plugin.logger;

import de.rayzs.pat.plugin.*;
import de.rayzs.pat.utils.Reflection;
import net.md_5.bungee.api.ProxyServer;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class Logger {

    private final static java.util.logging.Logger LOGGER =
            Reflection.isBungeecordServer()
                    ? BungeeLoader.getPluginLogger()
                    : BukkitLoader.getPluginLogger();

    public static void info(String text) { send(Priority.INFO, text); }
    public static void warning(String text) { send(Priority.WARNING, text); }

    protected static void send(Priority priority, String text) {
        boolean hasColors = text.contains("§");
        if(hasColors) {
            if (Reflection.isBungeecordServer()) ProxyServer.getInstance().getConsole().sendMessage(text);
            else Bukkit.getServer().getConsoleSender().sendMessage(text);
            return;
        }

        Level level = Level.parse(priority.name());
        LOGGER.log(level, text);
    }

    protected enum Priority { INFO, WARNING }
}