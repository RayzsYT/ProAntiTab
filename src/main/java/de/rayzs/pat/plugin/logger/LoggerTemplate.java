package de.rayzs.pat.plugin.logger;

import java.util.List;

public interface LoggerTemplate {

    void info(List<String> messages);
    void warn(List<String> messages);

    void send(LoggerPriority priority, List<String> messages);

    void info(String message);
    void warn(String message);

    void send(LoggerPriority priority, String message);
}
