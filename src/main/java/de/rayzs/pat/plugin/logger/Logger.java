package de.rayzs.pat.plugin.logger;

import de.rayzs.pat.plugin.logger.impl.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

import de.rayzs.pat.utils.*;
import java.net.URL;
import java.util.*;
import java.io.*;

public class Logger {

    private static final String LOG_HEADER;

    static {
        final String software = Reflection.isProxyServer()
                ? Reflection.isVelocityServer() ? "Velocity" : "Bungeecord"
                : (Reflection.isPaper() ? "Paper" : Reflection.isFoliaServer() ? "Folia" : "Spigot/Bukkit");

        final String version = Reflection.isProxyServer() ? "" : " " + Reflection.getRawVersionName().replace("_", ".");

        LOG_HEADER = "> ProAntiTab v" + version + " [" + software + version + "]";
    }

    private final static LoggerTemplate LOGGER =
            Reflection.isVelocityServer() ? new VelocityLogger()
                    : Reflection.isProxyServer() ? new BungeeLogger()
                    : new BukkitLogger();


    public static void info(List<String> messages) {
        LOGGER.info(messages);
    }

    public static void warning(List<String> messages) {
        LOGGER.warn(messages);
    }

    public static void debug(List<String> messages) {
        LOGGER.debug(messages);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warn(message);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static String post() throws IOException {
        LimitedList<String> clonedLogs = new LimitedList<>(LOGGER.getLogs());
        StringBuilder textBuilder = new StringBuilder(LOG_HEADER);

        clonedLogs.iterate(line -> {
            textBuilder.append("\n");
            textBuilder.append(line);
        });

        byte[] textInBytes = textBuilder.toString().getBytes(StandardCharsets.UTF_8);
        int postDataLength = textInBytes.length;
        URL url = new URL("https://haste.rayzs.de/documents");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        DataOutputStream outputStream;
        String response;

        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "ProAntiTab");
        connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        connection.setUseCaches(false);

        outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(textInBytes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        response = reader.readLine();

        if (response == null || !response.contains("\"key\"")) return null;
        response = response.substring(response.indexOf(":") + 2, response.length() - 2);
        return "https://haste.rayzs.de/" + response + ".txt";
    }
}
