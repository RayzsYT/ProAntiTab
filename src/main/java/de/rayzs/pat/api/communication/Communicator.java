package de.rayzs.pat.api.communication;

import java.util.*;
import java.util.concurrent.TimeUnit;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.client.impl.BungeeClientInfo;
import de.rayzs.pat.api.communication.client.impl.VelocityClientInfo;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;

public class Communicator {


    private static Communicator instance = null;

    public static void initialize(final Client client) {
        if (instance != null) {
            Logger.warning("Communicator instance is already initialized!");
            return;
        }

        instance = new Communicator(client);
    }

    public static Communicator get() {
        return instance;
    }


    public static class Backend2Proxy {

        private Backend2Proxy() {}

        public static void sendIdentityRequest() {
            get().send2Proxy(
                    new CommunicationPackets.Backend2Proxy.IdentityRequestPacket(get().getId())
            );
        }

        public static void sendKeepAliveRequest() {
            get().send2Proxy(
                    new CommunicationPackets.Backend2Proxy.KeepAlivePacket()
            );
        }

    }

    public static class Proxy2Backend {

        private Proxy2Backend() {}

        // Update commands for all players
        public static void sendUpdateCommand() {
            get().send2AllClients(
                    new CommunicationPackets.Proxy2Backend.UpdatePacket(null)
            );
        }

        // Update commands for a certain player.
        public static void sendUpdateCommand(UUID playerId, String serverName) {
            final ClientInfo clientInfo = get().clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.send(
                    new CommunicationPackets.Proxy2Backend.UpdatePacket(playerId)
            );
        }

        public static void sendDataSync() {
            Communicator.get().lastSync = System.currentTimeMillis();

            Communicator.get().send2AllClients(
                    Communicator.get().constructDataSyncPacket()
            );
        }

        // Sync data for one server
        public static void sendDataSync(String serverName) {
            final ClientInfo clientInfo = get().clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.send(
                    Communicator.get().constructDataSyncPacket()
            );
        }

        // Sync data for all registered clients.
        public static void sendConsoleMessage(String serverName, String message) {
            final ClientInfo clientInfo = get().clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.send(
                    new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket(message)
            );
        }

        public static void sendExecutePlayerCommand(String serverName, UUID playerId, String command) {
            final ClientInfo clientInfo = get().clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.send(
                    new CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket(playerId, command)
            );
        }

        public static void sendExecuteConsoleCommand(String serverName, String command) {
            final ClientInfo clientInfo = get().clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.send(
                    new CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket(command)
            );
        }

        public static void sendNotification(UUID playerId, String serverName, String command) {
            if (!Storage.ConfigSections.Settings.FORWARD_CONSOLE_NOTIFICATIONS.ENABLED) {
                return;
            }

            final ClientInfo clientInfo = get().clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.send(
                    new CommunicationPackets.Proxy2Backend.NotificationPacket(playerId, command)
            );
        }
    }


    private final Client client;
    private final UUID id = UUID.randomUUID();

    // Server name, ClientInfo
    private final Map<String, ClientInfo> clients = new HashMap<>();
    private final ExpireCache<String, ClientInfo> pendingClients = new ExpireCache<>(2, TimeUnit.SECONDS);


    private long lastReceivedKeepAliveResponse = System.currentTimeMillis();
    private long lastSync = System.currentTimeMillis();


    private Communicator(final Client client) {
        this.client = client;
    }


    // BACKEND -> PROXY
    public void handleB2PPacket(String serverName, CommunicationPackets.PATPacket incomingPacket) {
        if (!incomingPacket.tokenMatches(Storage.TOKEN)) {
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.KeepAlivePacket packet) {
            final ClientInfo clientInfo = clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.updateKeepAliveTime();
            clientInfo.send(new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket());

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.IdentityRequestPacket packet) {
            final ClientInfo registeredClientInfo = clients.get(serverName);

            final boolean exist = registeredClientInfo != null;
            final boolean same = exist && registeredClientInfo.getId().equals(packet.serverId());

            if (!same) {
                final ClientInfo clientInfo = Reflection.isVelocityServer()
                        ? new VelocityClientInfo(packet.serverId(), serverName)
                        : new BungeeClientInfo(packet.serverId(), serverName);

                pendingClients.put(serverName, clientInfo);
                clientInfo.send(new CommunicationPackets.Proxy2Backend.IdentityPacket(packet.serverId(), serverName));
            } else {
                registeredClientInfo.send(new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket());
                registeredClientInfo.send(new CommunicationPackets.Proxy2Backend.IdentityPacket(packet.serverId(), serverName));
            }

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.IdentityResponsePacket packet) {
            final ClientInfo clientInfo = pendingClients.get(serverName);

            if (clientInfo != null && clientInfo.getId().equals(packet.serverId())) {
                clientInfo.setId(packet.serverId());


                pendingClients.remove(serverName);
                clients.put(serverName, clientInfo);


                clientInfo.send(new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket());
                clientInfo.send( constructDataSyncPacket() );

                clientInfo.updateSyncTime();
            }

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.DataSyncReceivedPacket packet) {
            final ClientInfo clientInfo = clients.get(serverName);
            if (clientInfo == null) return;

            clientInfo.updateSyncTime();
        }

    }

    // PROXY -> BACKEND
    public void handleP2BPacket(CommunicationPackets.PATPacket incomingPacket) {
        if (!incomingPacket.tokenMatches(Storage.TOKEN)) {
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket packet) {

            lastReceivedKeepAliveResponse = System.currentTimeMillis();
            BackendUpdater.receivedKeepAlivePacket();

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.IdentityPacket packet) {
            Storage.SERVER_NAME = packet.serverName();

            send2Proxy(new CommunicationPackets.Backend2Proxy.IdentityResponsePacket(id));
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.DataSyncPacket packet) {
            lastSync = System.currentTimeMillis();

            send2Proxy(new CommunicationPackets.Backend2Proxy.DataSyncReceivedPacket());
            BukkitLoader.handleDataSyncPacket(packet);
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.UpdatePacket packet) {
            BukkitLoader.handleUpdateCommandsPacket(packet);
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.NotificationPacket packet) {
            BukkitLoader.handleNotificationPacket(packet);
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.ConsoleMessagePacket packet) {
            BukkitLoader.handleConsoleMessagePacket(packet);
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket packet) {
            BukkitLoader.handlePlayerExecuteCommandPacket(packet);
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket packet) {
            BukkitLoader.handleConsoleExecuteCommandPacket(packet);
            return;
        }
    }

    private void send2Proxy(CommunicationPackets.PATPacket packet) {
        if (Reflection.isProxyServer()) {
            Logger.warning("Cannot send B2P packet as proxy!");
            return;
        }

        client.send(packet);
    }

    private void send2Client(ClientInfo clientInfo, CommunicationPackets.PATPacket packet) {
        if (!Reflection.isProxyServer()) {
            Logger.warning("Cannot send P2B packet as backend!");
            return;
        }

        if (clientInfo == null || Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED) {
            return;
        }

        clientInfo.send(packet);
    }

    private void send2AllClients(CommunicationPackets.PATPacket packet) {
        if (!Reflection.isProxyServer()) {
            Logger.warning("Cannot send P2B packet as backend!");
            return;
        }

        if (Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED) {
            return;
        }

        getClients().forEach(clientInfo -> {
            clientInfo.send(packet);
        });
    }

    private CommunicationPackets.Proxy2Backend.DataSyncPacket constructDataSyncPacket() {
        return new CommunicationPackets.Proxy2Backend.DataSyncPacket(
                new CommunicationPackets.Proxy2Backend.DataSyncPacket.Messages(
                        Storage.ConfigSections.Messages.PREFIX.PREFIX
                ),
                new CommunicationPackets.Proxy2Backend.DataSyncPacket.AutoLowerCase(
                        Storage.ConfigSections.Settings.AUTO_LOWERCASE_COMMANDS.ENABLED
                ),
                new CommunicationPackets.Proxy2Backend.DataSyncPacket.UnknownCommand(
                        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED,
                        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE
                )
        );
    }

    public void reload() {
        this.client.reload();
    }

    public UUID getClientId(String serverName) {
        final ClientInfo clientInfo = clients.get(serverName);
        if (clientInfo == null) return null;

        return clientInfo.getId();
    }

    public boolean hasConnectedClients() {
        return !clients.isEmpty();
    }

    public Set<ClientInfo> getClients() {
        return new HashSet<>(clients.values());
    }

    public UUID getId() {
        return id;
    }

    public long getLastReceivedKeepAliveResponse() {
        return lastReceivedKeepAliveResponse;
    }

    public long getLastSync() {
        return lastSync;
    }

}
