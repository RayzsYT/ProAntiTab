package de.rayzs.pat.plugin.logger;

import de.rayzs.pat.plugin.*;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.ProxyServer;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class Logger {

    private final static java.util.logging.Logger LOGGER =
            Reflection.isVelocityServer() ? null
                    : Reflection.isProxyServer()
                    ? BungeeLoader.getPluginLogger()
                    : BukkitLoader.getPluginLogger();

    public static void info(String text) { send(Priority.INFO, text); }
    public static void warning(String text) { send(Priority.WARNING, text); }

    protected static void send(Priority priority, String text) {
        if(LOGGER == null) {
            System.out.println(text);
            return;
        }
        boolean hasColors = text.contains("§");
        if(hasColors) {
            if (Reflection.isProxyServer()) MessageTranslator.send(ProxyServer.getInstance().getConsole(), text);
            else MessageTranslator.send(Bukkit.getServer().getConsoleSender(), text);
            return;
        }

        Level level = Level.parse(priority.name());
        LOGGER.log(level, text);
    }

    protected enum Priority { INFO, WARNING }
}