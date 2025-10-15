package de.rayzs.pat.plugin.logger;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.plugin.logger.impl.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.permission.PermissionPlugin;

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

        LOG_HEADER = "> ProAntiTab v" + Storage.CURRENT_VERSION + " [" + software + version + "]";
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

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warn(message);
    }

    public static String post() throws IOException {

        byte[] textInBytes = (LOG_HEADER + "\n\n" + createInfo()).getBytes(StandardCharsets.UTF_8);
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

    private static String createInfo() {
        StringBuilder builder = new StringBuilder();

        builder.append("Updated: ")
                .append(!Storage.OUTDATED)
                .append("\n\n");

        builder.append("Mode: ")
                .append(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED ? "WHITELIST" : "BLACKLIST")
                .append("\n");

        builder.append("Detected Software-API: ")
                .append(Reflection.isProxyServer()
                        ? Reflection.isPaper() ? "Waterfall" :
                        Reflection.isVelocityServer() ? "Velocity" : "Bungeecord"
                        : Reflection.isFoliaServer() ? "Folia"
                        : Reflection.isPaper() ? "Paper" : "Spigot")
                .append("\n");

        builder.append("Installed plugins: \n  ")
                .append(String.join("\n  ", Storage.getLoader().getPluginNames()))
                .append("\n\n");

        builder.append("Detected supported plugins:")
                .append("\n");

        builder.append(" LuckPerms: ")
                .append(Storage.getPermissionPlugin() == PermissionPlugin.LUCKPERMS)
                .append("\n");

        if (!Reflection.isProxyServer()) {
            builder.append(" GroupManager: ")
                    .append(Storage.getPermissionPlugin() == PermissionPlugin.GROUPMANAGER)
                    .append("\n");
        }

        builder.append(" PlaceholderAPI: ")
                .append(Storage.USE_PLACEHOLDERAPI)
                .append("\n");

        builder.append(" ViaVersion: ")
                .append(Storage.USE_VIAVERSION)
                .append("\n\n");

        if (!Reflection.isProxyServer()) {
            builder.append("server-name: ")
                    .append(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED ? Storage.SERVER_NAME : "Not connected to a proxy using PAT")
                    .append("\n");

            builder.append("last-received-sync: ")
                    .append(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED ? Storage.LAST_SYNC : "Deactivated")
                    .append("\n");
        }

        builder.append("\ncommands: ")
                .append(String.join(", ", Storage.Blacklist.getBlacklist().getCommands()))
                .append("\n\n");

        if (Reflection.isProxyServer()) {
            builder.append("\nservers: ")
                    .append(String.join(", ", Storage.getServers()))
                    .append("\n");

            builder.append("last-sent-sync: ")
                    .append(Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED ? "Deactivated" : (System.currentTimeMillis() - Storage.LAST_SYNC))
                    .append("\n\n");

            for (ClientInfo client : Communicator.CLIENTS) {
                builder.append("synced-server: ")
                        .append(client.getName() + " (" + client.getSyncTime() + ")")
                        .append(", ");
            }

            builder.append("\n\n");
        }

        if (Reflection.isProxyServer()) {
            builder.append("server-commands:").append("\n");

            for (Map.Entry<String, GeneralBlacklist> blacklist : Storage.Blacklist.getBlacklists()) {
                builder.append("  ")
                        .append(blacklist.getKey()).append(": ")
                        .append(String.join(", ", blacklist.getValue().getCommands()))
                        .append("\n");
            }
        }

        builder.append("\ngroups:");
        for (Group group : GroupManager.getGroups()) {
            builder.append("\n")
                    .append(group.getGroupInfo());
        }

        return builder.toString();
    }
}
