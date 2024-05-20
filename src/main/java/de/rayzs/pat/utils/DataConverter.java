package de.rayzs.pat.utils;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import oracle.jrockit.jfr.jdkevents.ThrowableTracer;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class DataConverter {

    /*
        Code originally from:
        https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
     */

    public static byte[] convertToBytes(Object obj) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
            outputStream.writeObject(obj);
            outputStream.flush();
            return arrayOutputStream.toByteArray();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public static Object buildFromBytes(byte[] bytes) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
        try (ObjectInput input = new ObjectInputStream(arrayInputStream)) {
            return input.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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

    public static class PacketBundle implements CommunicationPacket, Serializable {

        private final CommandsPacket commandsPacket;
        private final GroupsPacket groupsPacket;
        private final UnknownCommandPacket unknownCommandPacket;
        private final String proxyToken, serverId;

        public PacketBundle(String proxyToken, String serverId, CommandsPacket commandsPacket, GroupsPacket groupsPacket) {
            this.proxyToken = proxyToken;
            this.serverId = serverId;
            this.commandsPacket = commandsPacket;
            this.groupsPacket = groupsPacket;
            this.unknownCommandPacket = new UnknownCommandPacket();
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

        private final List<Group> groups;

        public GroupsPacket(List<Group> groups) {
            this.groups = groups;
        }

        public List<Group> getGroups() {
            return groups;
        }
    }
}
