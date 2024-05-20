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
    public static long LAST_DATA_UPDATE = System.currentTimeMillis(), LAST_SENT_REQUEST = 0;
    public static int SERVER_DATA_SYNC_COUNT = 0;

    public static void sendPacket(Object packet) {
        CLIENT.send(packet);
    }

    public static void sendPacket(ClientInfo clientInfo, Object packet) {
        clientInfo.sendBytes(DataConverter.convertToBytes(packet));
    }

    public static void receiveInformation(String serverName, Object packet) {
        if(packet instanceof DataConverter.RequestPacket) {
            DataConverter.RequestPacket requestPacket = (DataConverter.RequestPacket) packet;
            if (!requestPacket.isToken(Storage.TOKEN)) return;

            ClientInfo client = getClientByName(serverName);
            if (client == null) {
                client = Reflection.isVelocityServer() ? new VelocityClientInfo(requestPacket.getServerId(), serverName) : new BungeeClientInfo(requestPacket.getServerId(), serverName);
                CLIENTS.add(client);
            }

            if (!client.compareId(requestPacket.getServerId())) client.setId(requestPacket.getServerId());

            syncData(client.getId());
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

            ClientInfo client = getClientByName(serverName);
            if (client == null) {
                client = Reflection.isVelocityServer() ? new VelocityClientInfo(feedbackPacket.getServerId(), serverName) : new BungeeClientInfo(feedbackPacket.getServerId(), serverName);
                CLIENTS.add(client);
            }

            if (!client.compareId(feedbackPacket.getServerId())) client.setId(feedbackPacket.getServerId());

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
        ClientInfo clientInfo;
        if(serverId == null) {
            resetAllFeedbacks();
            SERVER_DATA_SYNC_COUNT = 0;
            CLIENTS.forEach(currentClient -> syncData(currentClient.getId()));
            return;
        } else {
            clientInfo = getClientById(serverId);
            SERVER_DATA_SYNC_COUNT = SERVER_DATA_SYNC_COUNT -1;
            if(SERVER_DATA_SYNC_COUNT <= 0) SERVER_DATA_SYNC_COUNT = 0;
        }

        DataConverter.CommandsPacket commandsPacket = new DataConverter.CommandsPacket();
        DataConverter.GroupsPacket groupsPacket;
        DataConverter.PacketBundle bundle;
        List<String> commands = new ArrayList<>(Storage.BLACKLIST.getCommands());

        String serverName = clientInfo.getName();
        List<Group> groups = GroupManager.getGroupsByServer(serverName),
                newGroupList = new ArrayList<>();
        groups.forEach(oldGroup -> newGroupList.add(new Group(oldGroup.getGroupName(), oldGroup.getCommands(serverName))));
        groupsPacket = new DataConverter.GroupsPacket(GroupManager.getGroups());

        commandsPacket.setCommands(commands);
        bundle = new DataConverter.PacketBundle(Storage.TOKEN, serverId, commandsPacket, groupsPacket);
        clientInfo.sendBytes(DataConverter.convertToBytes(bundle));
    }

    public static void sendRequest() {
        if(System.currentTimeMillis() - LAST_SENT_REQUEST >= 5000) {
            LAST_SENT_REQUEST = System.currentTimeMillis();
            sendPacket(new DataConverter.RequestPacket(Storage.TOKEN, SERVER_ID.toString()));
        }
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

    public static ClientInfo getClientByName(String name) {
        return CLIENTS.isEmpty() ? null : CLIENTS.stream().filter(client -> client != null && client.getName().equals(name)).findFirst().orElse(null);
    }

    public static ClientInfo getQueueClientById(String id) {
        return QUEUE_CLIENTS.get(id);
    }

    public static void resetAllFeedbacks() {
        CLIENTS.forEach(client -> client.setFeedback(false));
    }

    public static List<String> getRegisteredServerNames() {
        List<String> result = new ArrayList<>();
        CLIENTS.stream().filter(client -> client != null && client.getName() != null).forEach(client -> result.add(client.getName()));
        return result;
    }
}
