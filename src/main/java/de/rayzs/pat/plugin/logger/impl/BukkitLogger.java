package de.rayzs.pat.plugin.logger.impl;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.LoggerPriority;
import de.rayzs.pat.plugin.logger.LoggerTemplate;
import de.rayzs.pat.utils.message.MessageTranslator;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BukkitLogger implements LoggerTemplate {

    private final List<String> LOGS = new ArrayList<>();
    private final java.util.logging.Logger logger = BukkitLoader.getPluginLogger();

    @Override
    public List<String> getLogs() {
        return LOGS;
    }

    @Override
    public void info(List<String> messages) {
        messages.forEach(this::info);
    }

    @Override
    public void warn(List<String> messages) {
        messages.forEach(this::warn);
    }

    @Override
    public void debug(List<String> messages) {
        messages.forEach(this::debug);
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
    public void debug(String message) {
        send(LoggerPriority.DEBUG, message);
    }

    @Override
    public void send(LoggerPriority priority, String message) {
        message = MessageTranslator.replaceMessage(message);

        String time = TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        if(time.length() != 12) time = time.substring(0, 9) + 0 + time.split(":")[3];
        LOGS.add("[" + priority.name() + " | " + time + "] " + MessageTranslator.colorless(message));

        if (priority == LoggerPriority.DEBUG)
            return;

        if (message.contains("§")) {
            MessageTranslator.send(Bukkit.getServer().getConsoleSender(), message);
            return;
        }

        switch (priority) {
            case INFO -> logger.info(message);
            case WARNING -> logger.warning(message);
        }
    }
}