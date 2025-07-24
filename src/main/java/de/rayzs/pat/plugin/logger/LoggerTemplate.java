package de.rayzs.pat.plugin.logger;

import java.text.SimpleDateFormat;
import java.util.List;

public interface LoggerTemplate {

    SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:SS");

    List<String> getLogs();

    void info(List<String> messages);
    void warn(List<String> messages);
    void debug(List<String> messages);

    void send(LoggerPriority priority, List<String> messages);

    void info(String message);
    void warn(String message);
    void debug(String message);

    void send(LoggerPriority priority, String message);
}
