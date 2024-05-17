package de.rayzs.pat.plugin.communication;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.plugin.communication.impl.*;
import java.util.*;

public class ClientCommunication {

    private static final Client CLIENT = Reflection.isVelocityServer() ? new VelocityClient() : Reflection.isProxyServer() ? new BungeeClient() : new BukkitClient();
    private static final UUID SERVER_ID = UUID.randomUUID();
    public static final List<ClientInfo> CLIENTS = new ArrayList<>();

    public static void sendInformation(String information) {
        CLIENT.sendInformation(information);
    }

    public static void receiveInformation(String serverName, String information) {
        if(!information.contains("::")) return;
        String[] args = information.split("::");

        switch (args[0].toLowerCase()) {
            case "request":
                if(args.length != 2 || !args[1].equals(Storage.TOKEN_KEY)) return;
                synchronizeInformation();
                break;
            case "received":
                if(args.length != 3 || !args[1].equals(Storage.TOKEN_KEY)) return;
                String id = args[2];
                ClientInfo client = getClientById(id);

                if(client == null) {
                    client = new ClientInfo(id, serverName);
                    CLIENTS.add(client);
                }
                else if(!client.getName().equals(serverName)) client.setName(serverName);

                if(!client.hasSentFeedback()) {
                    client.setFeedback(true);
                    client.syncTime();
                    Storage.SERVER_DATA_SYNC_COUNT++;
                    sendInformation("receive-info::" + Storage.TOKEN_KEY + "::" + client.getId() + "::" + client.getName());
                }
                break;
            case "receive-info":
                if(!args[2].equals(ClientCommunication.SERVER_ID.toString()) || Storage.SERVER_NAME != null || args.length != 4 || !args[1].equals(Storage.TOKEN_KEY)) return;
                Storage.SERVER_NAME = args[3];
                break;
            case "receive-commands":
                if(args.length != 4 || !args[1].equals(Storage.TOKEN_KEY)) return;
                BukkitLoader.synchronizeCommandData(Boolean.parseBoolean(args[2]), args[3].equals("§") ? new ArrayList<>() : Arrays.asList(args[3].split(";")));
                Storage.LAST_DATA_UPDATE = System.currentTimeMillis();
                break;
            case "receive-groups":
                if(args.length != 3 || !args[1].equals(Storage.TOKEN_KEY)) return;
                BukkitLoader.synchronizeGroupData(args[2]);
                Storage.LAST_DATA_UPDATE = System.currentTimeMillis();
                break;
        }
    }

    public static void synchronizeInformation() {
        Storage.LAST_DATA_UPDATE = System.currentTimeMillis();
        Storage.SERVER_DATA_SYNC_COUNT = 0;
        resetAllFeedbacks();

        sendInformation("receive-commands::" + Storage.TOKEN_KEY + "::" + Storage.TURN_BLACKLIST_TO_WHITELIST + "::" + (DataConverter.convertCommandsToString(Storage.BLOCKED_COMMANDS_LIST)));
        sendInformation("receive-groups::" + Storage.TOKEN_KEY + "::" + DataConverter.convertGroupsToString());
    }

    public static void sendRequest() {
        sendInformation("request::" + Storage.TOKEN_KEY);
    }

    public static void sendFeedback() {
        sendInformation("received::" + Storage.TOKEN_KEY + "::" + SERVER_ID);
    }

    public static Client getClient() {
        return CLIENT;
    }

    public static ClientInfo getClientById(String id) {
        return CLIENTS.stream().filter(client -> client.compareId(id)).findFirst().orElse(null);
    }

    public static void resetAllFeedbacks() {
        CLIENTS.forEach(client -> client.setFeedback(false));
    }
}
