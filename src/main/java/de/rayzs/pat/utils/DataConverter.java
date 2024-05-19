package de.rayzs.pat.utils;

import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.util.List;

public class DataConverter {

    /*
        Code originally from:
        https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
     */

    public static byte[] convertToBytes(PacketBundle bundle) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
            outputStream.writeObject(bundle);
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

    public static class PacketBundle {

        private final CommandsPacket commandsPacket;
        private final GroupsPacket groupsPacket;
        private final String proxyToken, serverId;

        public PacketBundle(String proxyToken, String serverId, CommandsPacket commandsPacket, GroupsPacket groupsPacket) {
            this.proxyToken = proxyToken;
            this.serverId = serverId;
            this.commandsPacket = commandsPacket;
            this.groupsPacket = groupsPacket;
        }

        public boolean isToken(String token) {
            return proxyToken.equals(token);
        }

        public boolean isId(String id) {
            return serverId.equals(id);
        }

        public CommandsPacket getCommandsPacket() {
            return commandsPacket;
        }

        public GroupsPacket getGroupsPacket() {
            return groupsPacket;
        }
    }

    public static class CommandsPacket {
        private final List<String> commands;

        public CommandsPacket(List<String> commands) {
            this.commands = commands;
        }

        public List<String> getCommands() {
            return commands;
        }
    }

    public static class GroupsPacket {
        private final String groupName;
        private final List<String> commands;

        public GroupsPacket(Group group) {
            this.groupName = group.getGroupName();
            this.commands = group.getCommands();
        }

        public String getGroupName() {
            return groupName;
        }

        public List<String> getCommands() {
            return commands;
        }
    }
}
