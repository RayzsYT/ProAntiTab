package de.rayzs.pat.utils.communication;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.DataConverter;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.utils.communication.impl.*;
import java.util.*;

public class ClientCommunication {

    private static final Client CLIENT = Reflection.isVelocityServer() ? null : Reflection.isProxyServer() ? new BungeeClient() : new BukkitClient();

    public static void sendInformation(String information) {
        if(CLIENT == null) return;
        CLIENT.sendInformation(information);
    }

    public static void receiveInformation(String information) {
        if(!information.contains("::")) return;
        String[] args = information.split("::");

        switch (args[0].toLowerCase()) {
            case "request":
                if(args.length != 2 || !args[1].equals(Storage.TOKEN)) return;
                BungeeLoader.handleIfConnectedToSpigot();
                synchronizeInformation();
                break;
            case "receive-commands":
                if(args.length != 3) return;
                BukkitLoader.synchronizeCommandData(Boolean.parseBoolean(args[1]), args[2].equals("§") ? new ArrayList<>() : Arrays.asList(args[2].split(";")));
                break;
            case "receive-groups":
                if(args.length != 2) return;
                BukkitLoader.synchronizeGroupData(args[1]);
                break;
        }
    }

    public static void synchronizeInformation() {
        sendInformation("receive-commands::" + Storage.TURN_BLACKLIST_TO_WHITELIST + "::" + (DataConverter.convertCommandsToString(Storage.BLOCKED_COMMANDS_LIST)));
        sendInformation("receive-groups::" + DataConverter.convertGroupsToString());
    }

    public static void sendRequest() {
        sendInformation("request::" + Storage.TOKEN);
    }

    public static Client getClient() {
        return CLIENT;
    }
}