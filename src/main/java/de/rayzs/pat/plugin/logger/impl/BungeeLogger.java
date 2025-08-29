package de.rayzs.pat.plugin.logger.impl;

import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.plugin.BungeeLoader;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.plugin.logger.*;
import java.util.*;

public class BungeeLogger implements LoggerTemplate {

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
        message = MessageTranslator.replaceMessage(message);

        if (message.contains("§")) {
            MessageTranslator.send(ProxyServer.getInstance().getConsole(), message);
            return;
        }

        switch (priority) {
            case INFO -> logger.info(message);
            case WARNING -> logger.warning(message);
        }
    }
}