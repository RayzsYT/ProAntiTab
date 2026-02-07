package de.rayzs.pat.api.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.client.impl.BungeeClientInfo;
import de.rayzs.pat.api.communication.client.impl.VelocityClientInfo;
import de.rayzs.pat.api.communication.impl.BukkitClient;
import de.rayzs.pat.api.communication.impl.BungeeClient;
import de.rayzs.pat.api.communication.impl.VelocityClient;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.group.TinyGroup;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.sender.CommandSender;

public class Communicator {

    private static final Client CLIENT =
            Reflection.isVelocityServer() ? new VelocityClient()
            : Reflection.isProxyServer() ? new BungeeClient()
            : new BukkitClient();

    private static final UUID SERVER_ID = UUID.randomUUID();

    public static final List<ClientInfo> CLIENTS = new ArrayList<>();

    public static int SERVER_DATA_SYNC_COUNT = 0;
    public static long LAST_DATA_UPDATE = System.currentTimeMillis(),
                        LAST_SENT_REQUEST = 0,
                        LAST_BUKKIT_SYNC = System.currentTimeMillis();

    public static void sendPacket(Object packet) {
        if (Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED) return;

        CLIENT.send(packet);
    }

    public static void sendPacket(ClientInfo clientInfo, Object packet) {
        if (Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED) return;
        if (clientInfo == null) return;

        clientInfo.sendBytes(CommunicationPackets.convertToBytes(packet));
    }

    public static void receiveInformation(String serverName, Object packet) {
        ClientInfo client = getClientByName(serverName);

        if (packet instanceof CommunicationPackets.RequestPacket requestPacket) {
            if (!requestPacket.isToken(Storage.TOKEN))
                return;

            if (client == null) {
                client = Reflection.isVelocityServer()
                        ? new VelocityClientInfo(requestPacket.getServerId(), serverName)
                        : new BungeeClientInfo(requestPacket.getServerId(), serverName);

                CLIENTS.add(client);
            }

            if (!client.compareId(requestPacket.getServerId()))
                client.setId(requestPacket.getServerId());

            syncData(client.getId());
            return;
        }

        if (packet instanceof CommunicationPackets.NotificationPacket notificationPacket) {
            if (!notificationPacket.isToken(Storage.TOKEN) || Reflection.isProxyServer()) {
                return;
            }

            BukkitLoader.handleNotificationPacket(notificationPacket);
        }

        if (packet instanceof CommunicationPackets.UpdateCommandsPacket updateCommandsPacket) {
            if (Reflection.isProxyServer())
                return;

            BukkitLoader.handleUpdateCommandsPacket(updateCommandsPacket);
        }

        if (packet instanceof CommunicationPackets.P2BExecutePacket p2BExecutePacket) {
            if (Reflection.isProxyServer())
                return;

            BukkitLoader.handleP2BExecute(p2BExecutePacket);
        }

        if (packet instanceof CommunicationPackets.P2BMessagePacket p2bMessagePacket) {
            if (Reflection.isProxyServer())
                return;

            BukkitLoader.handleP2BMessage(p2bMessagePacket);
        }

        if (packet instanceof CommunicationPackets.ForcePermissionResetPacket permissionResetPacket) {
            if (Reflection.isProxyServer())
                return;

            if (!permissionResetPacket.isToken(Storage.TOKEN))
                return;

            if (permissionResetPacket.hasTarget())
                PermissionUtil.setPlayerPermissions(permissionResetPacket.getTargetUUID());
            else
                PermissionUtil.reloadPermissions();

            return;
        }

        if (packet instanceof CommunicationPackets.BackendDataPacket backendDataPacket) {
            if (!backendDataPacket.isToken(Storage.TOKEN))
                return;

            Storage.SERVER_NAME = backendDataPacket.getServerName();
            return;
        }

        if (packet instanceof CommunicationPackets.FeedbackPacket feedbackPacket) {
            if (!feedbackPacket.isToken(Storage.TOKEN))
                return;

            String serverId = feedbackPacket.getServerId();

            if (client == null) {
                client = Reflection.isVelocityServer()
                        ? new VelocityClientInfo(feedbackPacket.getServerId(), serverName)
                        : new BungeeClientInfo(feedbackPacket.getServerId(), serverName);

                CLIENTS.add(client);
            }

            if (!client.compareId(feedbackPacket.getServerId()))
                client.setId(feedbackPacket.getServerId());

            if (client.hasSentFeedback())
                return;

            client.setFeedback(true);
            client.syncTime();
            SERVER_DATA_SYNC_COUNT++;

            CommunicationPackets.BackendDataPacket backendDataPacket = new CommunicationPackets.BackendDataPacket(Storage.TOKEN, serverId, serverName);
            sendPacket(client, backendDataPacket);
            return;
        }

        if (packet instanceof CommunicationPackets.PacketBundle packetBundle) {
            if (!packetBundle.isToken(Storage.TOKEN) || !packetBundle.isId(SERVER_ID.toString()))
                return;

            Storage.USE_VELOCITY = packetBundle.isVelocity();
            LAST_BUKKIT_SYNC = System.currentTimeMillis();
            BukkitLoader.synchronize(packetBundle);
        }
    }

    public static void syncData() {
        syncData(null);
    }

    public static void syncData(String serverId) {
        if (!Reflection.isProxyServer() || Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED)
            return;

        LAST_DATA_UPDATE = System.currentTimeMillis();
        ClientInfo clientInfo;

        if(serverId == null) {
            SERVER_DATA_SYNC_COUNT = 0;
            CLIENTS.forEach(currentClient -> syncData(currentClient.getId()));
            return;

        } else {
            clientInfo = getClientById(serverId);

            if (clientInfo != null)
                clientInfo.setFeedback(false);

            SERVER_DATA_SYNC_COUNT = Math.max(0, SERVER_DATA_SYNC_COUNT -1);
        }

        CommunicationPackets.CommandsPacket commandsPacket = new CommunicationPackets.CommandsPacket();
        CommunicationPackets.GroupsPacket groupsPacket;
        CommunicationPackets.PacketBundle bundle;
        List<String> commands = new ArrayList<>();
        String tempServerName = "";

        if (clientInfo != null && clientInfo.getName() != null) {
            tempServerName = clientInfo.getName();

            if (!Storage.Blacklist.isIgnoredServer(tempServerName))
                commands.addAll(Storage.Blacklist.getBlacklist().getCommands());

            List<GeneralBlacklist> serverBlacklists = Storage.Blacklist.getServerBlacklists(clientInfo.getName());
            serverBlacklists.stream().filter(serverBlacklist ->
                    serverBlacklist != null && serverBlacklist.getCommands() != null
            ).forEach(serverBlacklist ->
                    commands.addAll(serverBlacklist.getCommands())
            );
        }

        final String serverName = tempServerName;
        List<TinyGroup> groups = new ArrayList<>();

        TinyGroup tinyGroup;
        for (Group group : GroupManager.getGroups()) {
            if (groups.stream().anyMatch(cTG -> cTG.getGroupName().equals(group.getGroupName())))
                continue;

            tinyGroup = new TinyGroup(group.getGroupName(), group.getPriority(), group.getAllCommands(serverName));
            for (GroupBlacklist groupBlacklist : group.getAllServerGroupBlacklist(serverName))
                tinyGroup.addAll(groupBlacklist.getCommands());

            groups.add(tinyGroup);
        }

        groupsPacket = new CommunicationPackets.GroupsPacket(groups);
        commandsPacket.setTurnBlacklistToWhitelist(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
        commandsPacket.setCommands(commands);

        bundle = new CommunicationPackets.PacketBundle(Storage.TOKEN, serverId, commandsPacket, groupsPacket);
        clientInfo.sendBytes(CommunicationPackets.convertToBytes(bundle));

        PATEventHandler.callSentSyncEvents(bundle, serverName);
    }

    public static void sendRequest() {
        if (System.currentTimeMillis() - LAST_SENT_REQUEST >= 5000) {
            LAST_SENT_REQUEST = System.currentTimeMillis();
            sendPacket(new CommunicationPackets.RequestPacket(Storage.TOKEN, SERVER_ID.toString()));
        }
    }

    public static void sendPermissionReset() {
        if (Reflection.isProxyServer()) {
            sendPacket(new CommunicationPackets.ForcePermissionResetPacket(Storage.TOKEN));
        }
    }

    public static void sendP2BMessage(String serverName, String message) {
        if (Reflection.isProxyServer()) {
            ClientInfo client = getClientByName(serverName);
            sendPacket(client, new CommunicationPackets.P2BMessagePacket(Storage.TOKEN, message));
        }
    }

    public static void sendP2BExecute(String serverName, UUID targetUUID, String command) {
        if (Reflection.isProxyServer()) {
            ClientInfo client = getClientByName(serverName);
            sendPacket(client, new CommunicationPackets.P2BExecutePacket(Storage.TOKEN, targetUUID, command));
        }
    }

    public static void sendNotificationPacket(UUID targetUUID, String serverName, String displayedCommand) {
        if (!Storage.ConfigSections.Settings.FORWARD_CONSOLE_NOTIFICATIONS.ENABLED) {
            return;
        }

        for (ClientInfo client : CLIENTS) {
            if (client.getName().equalsIgnoreCase(serverName)) {
                sendPacket(client, new CommunicationPackets.NotificationPacket(Storage.TOKEN, targetUUID, displayedCommand));
                return;
            }
        }
    }

    public static void sendUpdateCommand() {
        if (Reflection.isProxyServer()) {
            sendPacket(new CommunicationPackets.UpdateCommandsPacket(Storage.TOKEN));
        }
    }

    public static void sendUpdateCommand(UUID targetUUID) {
        if (Reflection.isProxyServer()) {
            sendPacket(new CommunicationPackets.UpdateCommandsPacket(Storage.TOKEN, targetUUID));
        }
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
        return CLIENTS.isEmpty() ? null : CLIENTS.stream().filter(client -> client != null && client.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
