package de.rayzs.pat.plugin.system.communication;

import java.util.*;
import java.util.concurrent.TimeUnit;

import de.rayzs.pat.plugin.system.communication.client.Client;
import de.rayzs.pat.plugin.system.communication.client.impl.BungeeClient;
import de.rayzs.pat.plugin.system.communication.client.impl.VelocityClient;
import de.rayzs.pat.plugin.system.communication.cph.CommunicationPacketHandler;
import de.rayzs.pat.plugin.system.communication.pmc.PluginMessageClient;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;

public class Communicator {


    private static Communicator instance = null;

    public static void initialize(
            final PluginMessageClient pluginMessageClient,
            final CommunicationPacketHandler communicationPacketHandler
    ) {

        if (instance != null) {
            Logger.warning("Communicator instance is already initialized!");
            return;
        }

        instance = new Communicator(pluginMessageClient, communicationPacketHandler);
    }

    public static Communicator get() {
        return instance;
    }


    public static class Backend2Proxy {

        private Backend2Proxy() {}


        /**
         *
         * Fast and simple solution to simply send frequent used packets to the
         * proxy without any extra steps.
         *
         */


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

        /**
         *
         * Fast and quick solution to send frequently used packets to
         * the backend servers without any extra steps.
         *
         */

        // Update commands for all players
        public static void sendUpdateCommand() {
            get().send2AllClients(
                    new CommunicationPackets.Proxy2Backend.UpdatePacket(null)
            );
        }

        // Update commands for a certain player.
        public static void sendUpdateCommand(UUID playerId, String serverName) {
            final Client clientInfo = get().clients.get(serverName);
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
            final Client client = get().clients.get(serverName);
            if (client == null) return;

            client.send(
                    Communicator.get().constructDataSyncPacket()
            );
        }

        // Sync data for all registered clients.
        public static void sendConsoleMessage(String serverName, String message) {
            final Client client = get().clients.get(serverName);
            if (client == null) return;

            client.send(
                    new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket(message)
            );
        }

        public static void sendExecutePlayerCommand(String serverName, UUID playerId, String command) {
            final Client client = get().clients.get(serverName);
            if (client == null) return;

            client.send(
                    new CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket(playerId, command)
            );
        }

        public static void sendExecuteConsoleCommand(String serverName, String command) {
            final Client client = get().clients.get(serverName);
            if (client == null) return;

            client.send(
                    new CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket(command)
            );
        }

        public static void sendNotification(UUID playerId, String serverName, String command) {
            if (!Storage.ConfigSections.Settings.FORWARD_CONSOLE_NOTIFICATIONS.ENABLED) {
                return;
            }

            final Client client = get().clients.get(serverName);
            if (client == null) return;

            client.send(
                    new CommunicationPackets.Proxy2Backend.NotificationPacket(playerId, command)
            );
        }
    }


    private final PluginMessageClient pluginMessageClient;
    private final CommunicationPacketHandler communicationPacketHandler;
    private final UUID id = UUID.randomUUID();

    // Server name, ClientInfo
    private final Map<String, Client> clients = new HashMap<>();
    private final ExpireCache<String, Client> pendingClients = new ExpireCache<>(2, TimeUnit.SECONDS);


    private long lastReceivedKeepAliveResponse = System.currentTimeMillis();
    private long lastSync = System.currentTimeMillis();


    private Communicator(final PluginMessageClient pluginMessageClient, final CommunicationPacketHandler communicationPacketHandler) {
        this.pluginMessageClient = pluginMessageClient;
        this.communicationPacketHandler = communicationPacketHandler;
    }


    // BACKEND -> PROXY
    public void handleB2PPacket(String serverName, CommunicationPackets.PATPacket incomingPacket) {
        if (!incomingPacket.tokenMatches(Storage.TOKEN)) {
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.KeepAlivePacket packet) {
            final Client client = clients.get(serverName);
            if (client == null) return;

            client.updateKeepAliveTime();
            client.send(new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket());

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.IdentityRequestPacket packet) {
            final Client registeredClient = clients.get(serverName);

            final boolean exist = registeredClient != null;
            final boolean same = exist && registeredClient.getId().equals(packet.serverId());

            if (!same) {
                final Client client = createClientInstance(packet.serverId(), serverName);

                pendingClients.put(serverName, client);
                client.send(new CommunicationPackets.Proxy2Backend.IdentityPacket(packet.serverId(), serverName));
            } else {
                registeredClient.send(new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket());
                registeredClient.send(new CommunicationPackets.Proxy2Backend.IdentityPacket(packet.serverId(), serverName));
            }

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.IdentityResponsePacket packet) {
            final Client client = pendingClients.get(serverName);

            if (client != null && client.getId().equals(packet.serverId())) {
                client.setId(packet.serverId());


                pendingClients.remove(serverName);
                clients.put(serverName, client);


                client.send(new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket());
                client.send( constructDataSyncPacket() );

                client.updateSyncTime();
            }

            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.DataSyncReceivedPacket packet) {
            final Client client = clients.get(serverName);
            if (client == null) return;

            client.updateSyncTime();
            return;
        }


        // Handles all packets that can be handled with as pleased inside the CommunicationPacketHandler...
        communicationPacketHandler.handleReceivedPacket(incomingPacket);
    }

    // PROXY -> BACKEND
    public void handleP2BPacket(CommunicationPackets.PATPacket incomingPacket) {
        if (!incomingPacket.tokenMatches(Storage.TOKEN)) {
            return;
        }

        if (incomingPacket instanceof CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket packet) {

            lastReceivedKeepAliveResponse = System.currentTimeMillis();
            BackendUpdater.get().receivedKeepAlivePacket();

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
        }


        // Handles all packets that can be handled with as pleased inside the CommunicationPacketHandler...
        communicationPacketHandler.handleReceivedPacket(incomingPacket);
    }

    private void send2Proxy(CommunicationPackets.PATPacket packet) {
        if (Reflection.isProxyServer()) {
            Logger.warning("Cannot send B2P packet as proxy!");
            return;
        }

        pluginMessageClient.send(packet);
    }

    private void send2Client(Client clientInfo, CommunicationPackets.PATPacket packet) {
        if (!Reflection.isProxyServer()) {
            Logger.warning("Cannot send P2B packet as backend!");
            return;
        }

        if (clientInfo == null || Storage.ConfigSections.Settings.DISABLE_SYNC.ENABLED) {
            return;
        }

        clientInfo.send(packet);
    }

    private void send2AllClients(CommunicationPackets.PATPacket packet) {
        if (!Reflection.isProxyServer()) {
            Logger.warning("Cannot send P2B packet as backend!");
            return;
        }

        if (Storage.ConfigSections.Settings.DISABLE_SYNC.ENABLED) {
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
        this.pluginMessageClient.reload();
    }

    public UUID getClientId(String serverName) {
        final Client client = clients.get(serverName);
        if (client == null) return null;

        return client.getId();
    }

    public boolean hasConnectedClients() {
        return !clients.isEmpty();
    }

    public Set<Client> getClients() {
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

    private Client createClientInstance(UUID id, String serverName) {
        return Reflection.isVelocityServer()
                ? new VelocityClient(id, serverName)
                : new BungeeClient(id, serverName);
    }

}
