package de.rayzs.pat.api.communication;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.client.impl.BungeeClientInfo;
import de.rayzs.pat.api.communication.client.impl.VelocityClientInfo;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.communication.impl.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.DataConverter;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.Group;
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
                client = Reflection.isVelocityServer() ? new VelocityClientInfo(serverObj, requestPacket.getServerId(), serverName) : new BungeeClientInfo(serverObj, requestPacket.getServerId(), serverName);
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
                if(client != null) CLIENTS.add(client);
                else {
                    client = getClientById(feedbackPacket.getServerId());
                    if (client == null) {
                        client = Reflection.isVelocityServer() ? new VelocityClientInfo(serverObj, feedbackPacket.getServerId(), serverName) : new BungeeClientInfo(serverObj, feedbackPacket.getServerId(), serverName);
                        CLIENTS.add(client);
                    } else if (!client.getName().equals(serverName)) client.setName(serverName);
                }
            }

            if (client.hasSentFeedback()) return;
            client.setFeedback(true);
            client.syncTime();
            SERVER_DATA_SYNC_COUNT++;

            DataConverter.BackendDataPacket backendDataPacket = new DataConverter.BackendDataPacket(Storage.TOKEN, serverId, serverName);
            sendPacket(client, backendDataPacket);
            return;
        }

        if(packet instanceof DataConverter.PacketBundle) {
            DataConverter.PacketBundle packetBundle =  (DataConverter.PacketBundle) packet;
            if(!packetBundle.isToken(Storage.TOKEN) || !packetBundle.isId(SERVER_ID.toString())) return;

            DataConverter.CommandsPacket commandsPacket = packetBundle.getCommandsPacket();
            DataConverter.GroupsPacket groupsPacket = packetBundle.getGroupsPacket();
            DataConverter.UnknownCommandPacket unknownCommandPacket = packetBundle.getUnknownCommandPacket();

            BukkitLoader.synchronizeCommandData(commandsPacket, unknownCommandPacket);
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
        List<String> commands = new ArrayList<>(Storage.BLACKLIST.getCommands());

        if(clientInfo == null) sendPacket(bundle);
        else {
            String serverName = clientInfo.getName();
            List<Group> groups = GroupManager.getGroupsByServer(serverName);
            groups.forEach(group -> commands.addAll(group.getCommands(serverName)));
            commandsPacket.setCommands(commands);
            clientInfo.sendBytes(DataConverter.convertToBytes(bundle));
        }
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
        return CLIENTS.isEmpty() ? null : CLIENTS.stream().filter(client -> client != null && client.compareId(id)).findFirst().orElse(null);
    }

    public static ClientInfo getQueueClientById(String id) {
        return QUEUE_CLIENTS.get(id);
    }

    public static void resetAllFeedbacks() {
        CLIENTS.forEach(client -> client.setFeedback(false));
    }
}
