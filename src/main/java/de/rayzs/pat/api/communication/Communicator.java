package de.rayzs.pat.api.communication;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.client.impl.BungeeClientInfo;
import de.rayzs.pat.api.communication.client.impl.VelocityClientInfo;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.communication.impl.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.TinyGroup;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.permission.PermissionUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Communicator {

    private static final Client CLIENT = Reflection.isVelocityServer() ? new VelocityClient() : Reflection.isProxyServer() ? new BungeeClient() : new BukkitClient();
    private static final UUID SERVER_ID = UUID.randomUUID();
    public static final List<ClientInfo> CLIENTS = new ArrayList<>();
    private static final ExpireCache<String, ClientInfo> QUEUE_CLIENTS = new ExpireCache<>(15, TimeUnit.SECONDS);
    public static long LAST_DATA_UPDATE = System.currentTimeMillis(), LAST_SENT_REQUEST = 0, LAST_BUKKIT_SYNC = System.currentTimeMillis();
    public static int SERVER_DATA_SYNC_COUNT = 0;

    public static void sendPacket(Object packet) {
        CLIENT.send(packet);
    }

    public static void sendPacket(ClientInfo clientInfo, Object packet) {
        clientInfo.sendBytes(CommunicationPackets.convertToBytes(packet));
    }

    public static void receiveInformation(String serverName, Object packet) {
        if(packet instanceof CommunicationPackets.RequestPacket) {
            CommunicationPackets.RequestPacket requestPacket = (CommunicationPackets.RequestPacket) packet;
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

        if(packet instanceof CommunicationPackets.ForcePermissionResetPacket) {
            if(Reflection.isProxyServer()) return;
            CommunicationPackets.ForcePermissionResetPacket permissionResetPacket = (CommunicationPackets.ForcePermissionResetPacket) packet;
            if(!permissionResetPacket.isToken(Storage.TOKEN)) return;

            if(permissionResetPacket.hasTarget()) PermissionUtil.setPlayerPermissions(permissionResetPacket.getTargetUUID());
            else PermissionUtil.reloadPermissions();
            return;
        }

        if(packet instanceof CommunicationPackets.BackendDataPacket) {
            CommunicationPackets.BackendDataPacket backendDataPacket = (CommunicationPackets.BackendDataPacket) packet;
            if (!backendDataPacket.isToken(Storage.TOKEN)) return;
            Storage.SERVER_NAME = backendDataPacket.getServerName();
            return;
        }

        if(packet instanceof CommunicationPackets.FeedbackPacket) {
            CommunicationPackets.FeedbackPacket feedbackPacket = (CommunicationPackets.FeedbackPacket) packet;
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

            CommunicationPackets.BackendDataPacket backendDataPacket = new CommunicationPackets.BackendDataPacket(Storage.TOKEN, serverId, serverName);
            sendPacket(client, backendDataPacket);
            return;
        }

        if(packet instanceof CommunicationPackets.PacketBundle) {
            CommunicationPackets.PacketBundle packetBundle =  (CommunicationPackets.PacketBundle) packet;
            if(!packetBundle.isToken(Storage.TOKEN) || !packetBundle.isId(SERVER_ID.toString())) return;

            LAST_BUKKIT_SYNC = System.currentTimeMillis();
            BukkitLoader.synchronize(packetBundle);
        }
    }

    public static void syncData() {
        syncData(null);
    }

    public static void syncData(String serverId) {
        LAST_DATA_UPDATE = System.currentTimeMillis();
        ClientInfo clientInfo;
        if(serverId == null) {
            SERVER_DATA_SYNC_COUNT = 0;
            CLIENTS.forEach(currentClient -> syncData(currentClient.getId()));
            return;
        } else {
            clientInfo = getClientById(serverId);
            if(clientInfo != null) clientInfo.setFeedback(false);

            SERVER_DATA_SYNC_COUNT = SERVER_DATA_SYNC_COUNT -1;
            if(SERVER_DATA_SYNC_COUNT <= 0) SERVER_DATA_SYNC_COUNT = 0;
        }

        CommunicationPackets.CommandsPacket commandsPacket = new CommunicationPackets.CommandsPacket();
        CommunicationPackets.GroupsPacket groupsPacket;
        CommunicationPackets.PacketBundle bundle;
        List<String> commands = new ArrayList<>(Storage.Blacklist.getBlacklist().getCommands());

        if(clientInfo != null && clientInfo.getName() != null) {
            List<GeneralBlacklist> serverBlacklists = Storage.Blacklist.getBlacklists(clientInfo.getName());
            serverBlacklists.stream().filter(serverBlacklist -> serverBlacklist != null && serverBlacklist.getCommands() != null).forEach(serverBlacklist -> commands.addAll(serverBlacklist.getCommands()));
        }

        String serverName = clientInfo.getName();
        List<TinyGroup> groups = new ArrayList<>();

        TinyGroup tinyGroup;
        for (Group group : GroupManager.getGroups()) {
            if(groups.stream().anyMatch(cTG -> cTG.getGroupName().equals(group.getGroupName()))) continue;

            tinyGroup = new TinyGroup(group.getGroupName(), group.getAllCommands(serverName));
            for (GroupBlacklist groupBlacklist : group.getAllServerGroupBlacklist(serverName))
                tinyGroup.addAll(groupBlacklist.getCommands());

            groups.add(tinyGroup);
        }

        groupsPacket = new CommunicationPackets.GroupsPacket(groups);
        commandsPacket.setTurnBlacklistToWhitelist(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
        commandsPacket.setCommands(commands);

        bundle = new CommunicationPackets.PacketBundle(Storage.TOKEN, serverId, commandsPacket, groupsPacket);
        clientInfo.sendBytes(CommunicationPackets.convertToBytes(bundle));
    }

    public static void sendRequest() {
        if(System.currentTimeMillis() - LAST_SENT_REQUEST >= 5000) {
            LAST_SENT_REQUEST = System.currentTimeMillis();
            sendPacket(new CommunicationPackets.RequestPacket(Storage.TOKEN, SERVER_ID.toString()));
        }
    }

    public static void sendPermissionReset() {
        if(Reflection.isProxyServer())
            sendPacket(new CommunicationPackets.ForcePermissionResetPacket(Storage.TOKEN));
    }

    public static void sendFeedback(UUID targetUUID) {
        if(Reflection.isProxyServer())
            sendPacket(new CommunicationPackets.ForcePermissionResetPacket(Storage.TOKEN, targetUUID));
    }

    public static void sendFeedback() {
        sendPacket(new CommunicationPackets.FeedbackPacket(Storage.TOKEN, SERVER_ID.toString()));
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
