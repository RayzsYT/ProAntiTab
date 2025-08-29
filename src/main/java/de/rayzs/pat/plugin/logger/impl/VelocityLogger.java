package de.rayzs.pat.plugin.logger.impl;

import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.plugin.logger.*;
import org.slf4j.Logger;
import java.util.*;

public class VelocityLogger implements LoggerTemplate {

    private final Logger logger = VelocityLoader.getPluginLogger();

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
            MessageTranslator.send(VelocityLoader.getServer().getConsoleCommandSource(), message);
            return;
        }

        switch (priority) {
            case INFO -> logger.info(message);
            case WARNING -> logger.warn(message);
        }
    }
}