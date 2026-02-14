package de.rayzs.pat.utils;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.*;
import java.io.*;

public class CommunicationPackets {


    private static final byte INIT_PACKET = 0;
    private static final byte NOT_INIT_PACKET = 1;


    public static byte[] preparePacket(Object object) {
        return preparePacket(object, Communicator.get().getId());
    }

    public static byte[] preparePacket(Object object, UUID id) {
        if (!isValidPacket(object)) {
            return null;
        }

        final boolean isInitialPacket = isInitialPacket(object);
        final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

        try (ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream)) {
            outputStream.writeObject(object);
            outputStream.flush();

            final byte[] encryptedBytes = xor(arrayOutputStream.toByteArray(), isInitialPacket
                    ? Storage.TOKEN
                    : id.toString()
            );

            final byte[] bytes = new byte[encryptedBytes.length + 1];

            bytes[0] = isInitialPacket ? INIT_PACKET : NOT_INIT_PACKET;
            System.arraycopy(encryptedBytes, 0, bytes, 1, bytes.length - 1);

            return bytes;

        } catch (Exception ignored) { }

        return null;
    }

    public static Object readPacket(byte[] bytes) {
        return readPacket(bytes, Communicator.get().getId());
    }

    public static Object readPacket(byte[] bytes, UUID id) {
        if (bytes.length == 0) {
            return null;
        }

        final byte init = bytes[0];
        bytes = Arrays.copyOfRange(bytes, 1, bytes.length);

        bytes = xor(bytes, isInitialPacket(init)
                ? Storage.TOKEN
                : id.toString()
        );

        final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);

        try (ObjectInput input = new ObjectInputStream(arrayInputStream)) {
            final Object object = input.readObject();

            if (object.getClass() == null || !isValidPacket(object)) {
                return null;
            }

            return object;
        } catch (Exception ignored) { }

        return null;
    }

    // Yes, I am aware that xor is not the best encryption out there,
    // but it will suffice for now since the encryption shouldn't be too heavy.
    private static byte[] xor(byte[] bytes, String key) {
        final byte[] output = new byte[bytes.length];
        final byte[] secret = key.getBytes();

        int pos = 0;

        for (int i = 0; i < bytes.length; i++) {
            output[i] = (byte) (bytes[i] ^ secret[pos]);
            pos += 1;

            if (pos >= secret.length) {
                pos = 0;
            }
        }

        return output;
    }

    public static boolean isValidPacket(Object object) {
        return object instanceof PATPacket;
    }

    public static boolean isP2BPacket(Object object) {
        return object instanceof P2BPacket;
    }

    public static boolean isB2PPacket(Object object) {
        return object instanceof B2PPacket;
    }

    public static boolean isInitialPacket(Object object) {
        return object instanceof InitialPacket;
    }

    public static boolean isInitialPacket(byte[] bytes) {
        return bytes.length > 0 && isInitialPacket(bytes[0]);
    }

    public static boolean isInitialPacket(byte b) {
        return b == INIT_PACKET;
    }

    public interface PATPacket extends Serializable {
        String token = Storage.TOKEN;

        default boolean tokenMatches(String token) {
            return PATPacket.token.equals(token);
        }
    }


    private interface InitialPacket extends PATPacket {}
    private interface Synchronizable extends Serializable {}

    private interface P2BPacket extends PATPacket {}
    private interface B2PPacket extends PATPacket {}


    public static class Proxy2Backend {

        private Proxy2Backend() { }

        public record UpdatePacket(UUID playerId) implements P2BPacket {
            public boolean forEveryone() { return playerId == null; }
        }

        public record IdentityPacket(UUID targetServerId, String serverName) implements InitialPacket, P2BPacket { }

        public record NotificationPacket(UUID playerId, String command) implements P2BPacket { }

        public record ExecuteConsoleCommandPacket(String command) implements P2BPacket { }

        public record ExecutePlayerCommandPacket(UUID playerId, String command) implements P2BPacket { }

        public record ConsoleMessagePacket(String message) implements P2BPacket { }

        public record KeepAliveResponsePacket() implements P2BPacket { }

        public record DataSyncPacket(
                Messages messages,
                AutoLowerCase autoLowerCase,
                UnknownCommand unknownCommand
        ) implements P2BPacket {

            public record Messages(String prefix)
                    implements Synchronizable {}

            public record AutoLowerCase(boolean enabled)
                    implements Synchronizable {}

            public record UnknownCommand(boolean enabled, MultipleMessagesHelper message)
                    implements Synchronizable {}

        }

    }

    public static class Backend2Proxy {

        private Backend2Proxy() { }

        public record IdentityRequestPacket(UUID serverId) implements InitialPacket, B2PPacket { }

        public record IdentityResponsePacket(UUID serverId) implements InitialPacket, B2PPacket { }

        public record DataSyncReceivedPacket() implements B2PPacket { }

        public record KeepAlivePacket() implements B2PPacket { }

    }
}
