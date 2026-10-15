package de.rayzs.pat.plugin.logger.impl;

import de.rayzs.pat.utils.message.MessageHelper;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.sender.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.plugin.logger.*;

import java.util.*;

public class BungeeLogger implements LoggerTemplate {

    private final CommandSender console = CommandSender.from(ProxyServer.getInstance().getConsole());
    private final java.util.logging.Logger logger = BungeeLoader.getPluginLogger();

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