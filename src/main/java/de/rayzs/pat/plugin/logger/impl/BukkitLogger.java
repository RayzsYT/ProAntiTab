package de.rayzs.pat.plugin.logger.impl;

import de.rayzs.pat.utils.message.MessageHelper;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.*;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitLogger implements LoggerTemplate {

    private final CommandSender console = CommandSender.from(Bukkit.getServer().getConsoleSender());
    private final java.util.logging.Logger logger = BukkitLoader.getPluginLogger();

    @Override
    public void info(List<String> messages) {
        messages.forEach(this::info);
    }

    @Override
    public void warn(List<String> messages) {
        messages.forEach(this::warn);
    }

    @Override
    public void send(LoggerPriority priority, List<String> messages) {
        messages.forEach(message -> send(priority, message));
    }

    @Override
    public void info(String message) {
        send(LoggerPriority.INFO, message);
    }

    @Override
    public void warn(String message) {
        send(LoggerPriority.WARNING, message);
    }

    @Override
    public void send(LoggerPriority priority, String message) {
        message = PlaceholderReplacer.applyPlaceholders(null, message);

        if (message.contains("§")) {
            MessageHelper.sendMessage(console, message);
            return;
        }

        switch (priority) {
            case INFO -> logger.info(message);
            case WARNING -> logger.warning(message);
        }
    }
}