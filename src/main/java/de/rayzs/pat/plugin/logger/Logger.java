package de.rayzs.pat.plugin.logger;

import de.rayzs.pat.utils.message.MessageTranslator;
import java.nio.charset.StandardCharsets;
import de.rayzs.pat.api.storage.Storage;
import javax.net.ssl.HttpsURLConnection;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.utils.Reflection;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import de.rayzs.pat.plugin.*;
import org.bukkit.Bukkit;
import java.net.URL;
import java.util.*;
import java.io.*;

public class Logger {

    private static final ArrayList<String> LOGS = new ArrayList<>();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:SS"), DATE_FORMAT = new SimpleDateFormat("yy:MM:dd");

    private final static java.util.logging.Logger LOGGER =
            Reflection.isVelocityServer() ? null
                    : Reflection.isProxyServer()
                    ? BungeeLoader.getPluginLogger()
                    : BukkitLoader.getPluginLogger();

    public static void info(List<String> texts) { texts.forEach(text -> send(Priority.INFO, text)); }
    public static void warning(List<String> texts) { texts.forEach(text -> send(Priority.WARNING, text)); }
    public static void info(String text) { send(Priority.INFO, text); }
    public static void warning(String text) { send(Priority.WARNING, text); }

    protected static void send(Priority priority, String text) {
        text = MessageTranslator.replaceMessage(text);

        String time = TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        if(time.length() != 12) time = time.substring(0, 9) + 0 + time.split(":")[3];
        LOGS.add("[" + priority.name() + " |" + time + "] " + MessageTranslator.colorless(text));

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

    public static String post() throws IOException {
        ArrayList<String> clonedLogs = (ArrayList<String>) LOGS.clone();
        int startIndex = clonedLogs.size() > 980 ? clonedLogs.size() - 980 : 0;
        StringBuilder textBuilder = new StringBuilder("[ProAntiTab " + Storage.CURRENT_VERSION + " | " + (Reflection.isProxyServer() ? Reflection.isVelocityServer() ? "Velocity" : "Proxy" : Reflection.isPaper() ? "Paper" : "Bukkit") + "] Server version: " + Reflection.getVersionName() + " " + Reflection.getRawVersionName().replace("_", ".") + "\n" + (startIndex != 0 ? "... another part is split!\n\n" : ""));

        for(int i = startIndex; i < clonedLogs.size(); i++) {
            if (clonedLogs.get(i) == null) continue;
            String line = clonedLogs.get(i);
            textBuilder.append("\n");
            textBuilder.append(line);
        }

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

    protected enum Priority { INFO, WARNING }
}