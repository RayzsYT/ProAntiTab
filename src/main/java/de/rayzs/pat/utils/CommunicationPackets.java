package de.rayzs.pat.utils;

import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import de.rayzs.pat.utils.group.TinyGroup;
import de.rayzs.pat.api.storage.Storage;
import java.util.*;
import java.io.*;

public class CommunicationPackets {

    public static byte[] convertToBytes(Object obj) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
            outputStream.writeObject(obj);
            outputStream.flush();
            return arrayOutputStream.toByteArray();
        } catch (Exception ignored) { }

        return null;
    }

    public static Object buildFromBytes(byte[] bytes) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
        try (ObjectInput input = new ObjectInputStream(arrayInputStream)) {
            Object object = input.readObject();
            if (object.getClass() == null) return null;
            return object;
        } catch (Throwable ignored) { }

        return null;
    }

    public static boolean isPacket(Object object) {
        return object instanceof CommunicationPacket;
    }

    public interface CommunicationPacket {}

    public static class BackendDataPacket implements CommunicationPacket, Serializable {
        private final String proxyToken, serverName, serverId;

        public BackendDataPacket(String proxyToken, String serverId, String serverName) {
            this.proxyToken = proxyToken;
            this.serverId = serverId;
            this.serverName = serverName;
        }

        public String getServerId() {
            return serverId;
        }

        public String getServerName() {
            return serverName;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class UpdateCommandsPacket implements CommunicationPacket, Serializable {
        private final String proxyToken;
        private final UUID targetUUID;

        public UpdateCommandsPacket(String proxyToken) {
            this.proxyToken = proxyToken;
            this.targetUUID = null;
        }

        public UpdateCommandsPacket(String proxyToken, UUID targetUUID) {
            this.proxyToken = proxyToken;
            this.targetUUID = targetUUID;
        }

        public boolean hasTargetUUID() {
            return targetUUID != null;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    // Proxy to Backend (P2B)
    public static class P2BMessagePacket implements CommunicationPacket, Serializable {
        private final String proxyToken, message;

        public P2BMessagePacket(String proxyToken, String message) {
            this.proxyToken = proxyToken;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class P2BExecutePacket implements CommunicationPacket, Serializable {
        private final String proxyToken, command;
        private final UUID targetUUID;

        public P2BExecutePacket(String proxyToken, UUID targetUUID, String command) {
            this.proxyToken = proxyToken;
            this.targetUUID = targetUUID;
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public boolean isConsole() {
            return !isPlayer();
        }

        public boolean isPlayer() {
            return targetUUID != null;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class FeedbackPacket implements CommunicationPacket, Serializable {
        private final String proxyToken, serverId;

        public FeedbackPacket(String proxyToken, String serverId) {
            this.proxyToken = proxyToken;
            this.serverId = serverId;
        }

        public String getServerId() {
            return serverId;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class RequestPacket implements CommunicationPacket, Serializable {
        private final String proxyToken, serverId;

        public RequestPacket(String proxyToken, String serverId) {
            this.proxyToken = proxyToken;
            this.serverId = serverId;
        }

        public String getServerId() {
            return serverId;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class NotificationPacket implements CommunicationPacket, Serializable {

        private final String proxyToken, displayedCommand;
        private final UUID targetUUID;

        public NotificationPacket(String proxyToken, UUID targetUUID, String displayedCommand) {
            this.proxyToken = proxyToken;
            this.targetUUID = targetUUID;
            this.displayedCommand = displayedCommand;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        public String getDisplayedCommand() {
            return displayedCommand;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class ForcePermissionResetPacket implements Serializable {

        private final String proxyToken;
        private UUID targetUUID = null;

        public ForcePermissionResetPacket(String proxyToken) {
            this.proxyToken = proxyToken;
        }

        public ForcePermissionResetPacket(String proxyToken, UUID targetUUID) {
            this.proxyToken = proxyToken;
            this.targetUUID = targetUUID;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        public boolean hasTarget() {
            return targetUUID != null;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }
    }

    public static class PacketBundle implements CommunicationPacket, Serializable {

        private final CommandsPacket commandsPacket;
        private final GroupsPacket groupsPacket;
        private final UnknownCommandPacket unknownCommandPacket;
        private final NamespaceCommandsPacket namespaceCommandsPacket;
        private final MessagePacket messagePacket;
        private final String proxyToken, serverId;
        private final boolean velocity;

        public PacketBundle(String proxyToken, String serverId, CommandsPacket commandsPacket, GroupsPacket groupsPacket) {
            this.proxyToken = proxyToken;
            this.serverId = serverId;
            this.commandsPacket = commandsPacket;
            this.groupsPacket = groupsPacket;
            this.unknownCommandPacket = new UnknownCommandPacket();
            this.namespaceCommandsPacket = new NamespaceCommandsPacket();
            this.messagePacket = new MessagePacket();
            this.velocity = Reflection.isVelocityServer();
        }

        public boolean isVelocity() {
            return velocity;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }

        public boolean isId(String id) {
            return idIgnored() || serverId.equals(id);
        }

        public boolean idIgnored() {
            return serverId == null;
        }

        public CommandsPacket getCommandsPacket() {
            return commandsPacket;
        }

        public GroupsPacket getGroupsPacket() {
            return groupsPacket;
        }

        public UnknownCommandPacket getUnknownCommandPacket() { return unknownCommandPacket; }

        public MessagePacket getMessagePacket() {
            return messagePacket;
        }

        public NamespaceCommandsPacket getNamespaceCommandsPacket() {
            return namespaceCommandsPacket;
        }
    }

    public static class MessagePacket implements Serializable {

        private final String prefix;
        private final MultipleMessagesHelper baseBlockedMessage, subBlockedMessage;

        public MessagePacket() {
            prefix = Storage.ConfigSections.Messages.PREFIX.PREFIX;
            baseBlockedMessage = Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE;
            subBlockedMessage = Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE;
        }

        public String getPrefix() {
            return prefix;
        }

        public MultipleMessagesHelper getBaseBlockedMessage() {
            return baseBlockedMessage;
        }

        public MultipleMessagesHelper getSubBlockedMessage() {
            return subBlockedMessage;
        }

    }

    public static class NamespaceCommandsPacket implements Serializable {

        private final boolean enabled;

        public NamespaceCommandsPacket() {
            enabled = Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.ENABLED;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class UnknownCommandPacket implements Serializable {

        private final MultipleMessagesHelper message;
        private final boolean enabled;

        public UnknownCommandPacket() {
            enabled = Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED;
            message = Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE;
        }

        public MultipleMessagesHelper getMessage() {
            return message;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class CommandsPacket implements Serializable {

        private List<String> commands;
        private boolean turnBlacklistToWhitelist;

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }

        public void setTurnBlacklistToWhitelist(boolean turnBlacklistToWhitelist) {
            this.turnBlacklistToWhitelist = turnBlacklistToWhitelist;
        }

        public boolean turnBlacklistToWhitelistEnabled() {
            return turnBlacklistToWhitelist;
        }

        public List<String> getCommands() {
            return commands;
        }
    }

    public static class GroupsPacket implements Serializable {

        private final List<TinyGroup> groups;

        public GroupsPacket(List<TinyGroup> groups) {
            this.groups = groups;
        }

        public List<TinyGroup> getGroups() {
            return groups;
        }
    }
}
