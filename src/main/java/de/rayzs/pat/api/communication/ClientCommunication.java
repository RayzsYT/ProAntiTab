package de.rayzs.pat.api.communication;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.communication.impl.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.DataConverter;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClientCommunication {

    private static final Client CLIENT = Reflection.isVelocityServer() ? new VelocityClient() : Reflection.isProxyServer() ? new BungeeClient() : new BukkitClient();
    private static final UUID SERVER_ID = UUID.randomUUID();
    public static final List<ClientInfo> CLIENTS = new ArrayList<>();
    private static final ExpireCache<String, ClientInfo> QUEUE_CLIENTS = new ExpireCache<>(15, TimeUnit.SECONDS);
    private static long LAST_DATA_UPDATE = System.currentTimeMillis();
    private static int SERVER_DATA_SYNC_COUNT = 0;

    public static void sendPacket(Object packet) {
        CLIENT.send(packet);
    }

    public static void sendPacket(ClientInfo clientInfo, Object packet) {
        clientInfo.sendBytes(DataConverter.convertToBytes(packet));
    }

    public static void receiveInformation(String serverName, Object packet, Object serverObj) {
        if(packet instanceof DataConverter.RequestPacket) {
            DataConverter.RequestPacket requestPacket = (DataConverter.RequestPacket) packet;
            if (!requestPacket.isToken(Storage.TOKEN)) return;

            ClientInfo client = getClientById(requestPacket.getServerId());
            if (client == null) {
                client = new ClientInfo(requestPacket.getServerId(), serverName);
                QUEUE_CLIENTS.put(requestPacket.getServerId(), client);
            } else if (!client.getName().equals(serverName)) client.setName(serverName);


            syncData(requestPacket.getServerId());
            return;
        }

        if(packet instanceof DataConverter.BackendDataPacket) {
            DataConverter.BackendDataPacket backendDataPacket = (DataConverter.BackendDataPacket) packet;
            if (!backendDataPacket.isToken(Storage.TOKEN)) return;
            Storage.SERVER_NAME = backendDataPacket.getServerName();
            return;
        }

        if(packet instanceof DataConverter.FeedbackPacket) {
            DataConverter.FeedbackPacket feedbackPacket = (DataConverter.FeedbackPacket) packet;
            if (!feedbackPacket.isToken(Storage.TOKEN)) return;
            String serverId = feedbackPacket.getServerId();

            ClientInfo client = getClientById(serverId);
            if(client == null) {
                client = getQueueClientById(serverId);
                if(serverId != null) CLIENTS.add(client);
                else return;
            }

            if (client == null || client.hasSentFeedback()) return;
            client.setFeedback(true);
            client.syncTime();
            SERVER_DATA_SYNC_COUNT++;

            DataConverter.BackendDataPacket backendDataPacket = new DataConverter.BackendDataPacket(Storage.TOKEN, serverId, serverName);
            sendPacket(client, backendDataPacket);
            return;
        }

        if(packet instanceof DataConverter.PacketBundle) {
            DataConverter.BackendDataPacket backendDataPacket = (DataConverter.BackendDataPacket) packet;
            if(!backendDataPacket.isToken(Storage.TOKEN)) return;
            DataConverter.PacketBundle packetBundle =  (DataConverter.PacketBundle) packet;
            DataConverter.CommandsPacket commandsPacket = packetBundle.getCommandsPacket();
            DataConverter.GroupsPacket groupsPacket = packetBundle.getGroupsPacket();

            BukkitLoader.synchronizeCommandData(commandsPacket);
            BukkitLoader.synchronizeGroupData(groupsPacket);
        }
    }

    public static void syncData() {
        syncData(null);
    }

    public static void syncData(String serverId) {
        LAST_DATA_UPDATE = System.currentTimeMillis();
        ClientInfo clientInfo = null;
        if(serverId == null) {
            resetAllFeedbacks();
            SERVER_DATA_SYNC_COUNT = 0;
        } else {
            clientInfo = getClientById(serverId);
            SERVER_DATA_SYNC_COUNT = SERVER_DATA_SYNC_COUNT -1;
        }

        DataConverter.CommandsPacket commandsPacket = new DataConverter.CommandsPacket();
        DataConverter.GroupsPacket groupsPacket = new DataConverter.GroupsPacket(GroupManager.getGroups());
        DataConverter.PacketBundle bundle = new DataConverter.PacketBundle(Storage.TOKEN, serverId, commandsPacket, groupsPacket);

        if(clientInfo == null) sendPacket(bundle);
        else clientInfo.sendBytes(DataConverter.convertToBytes(bundle));
    }

    public static void sendRequest() {
        sendPacket(new DataConverter.RequestPacket(Storage.TOKEN, SERVER_ID.toString()));
    }
    public static void sendFeedback() {
        sendPacket(new DataConverter.FeedbackPacket(Storage.TOKEN, SERVER_ID.toString()));
    }

    public static Client getClient() {
        return CLIENT;
    }

    public static ClientInfo getClientById(String id) {
        return CLIENTS.stream().filter(client -> client.compareId(id)).findFirst().orElse(null);
    }

    public static ClientInfo getQueueClientById(String id) {
        return QUEUE_CLIENTS.get(id);
    }

    public static void resetAllFeedbacks() {
        CLIENTS.forEach(client -> client.setFeedback(false));
    }
}
