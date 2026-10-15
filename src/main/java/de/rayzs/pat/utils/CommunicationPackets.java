package de.rayzs.pat.utils;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.system.communication.Communicator;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Wire codec for communication between proxy and backend servers.
 *
 * <p>Packets are explicitly encoded and protected with AES-256-GCM before
 * transmission. Encryption keys are derived from {@link Storage#TOKEN},
 * using backend UUIDs only as derivation context.
 *
 * <p>The codec does not use native Java deserialization for network data,
 * and all variable-length fields are bounded before allocation.
 */
public class CommunicationPackets {

    public static final byte PROTOCOL_VERSION = 1;
    private static final byte INIT_PACKET = 0;
    private static final byte NOT_INIT_PACKET = 1;

    private static final int HEADER_LENGTH = 2;
    private static final int NONCE_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_TAG_LENGTH = GCM_TAG_BITS / 8;
    // Velocity defaults to 32,767 bytes for plugin message payloads.
    private static final int MAX_WIRE_BYTES = 32_767;
    private static final int MAX_PLAINTEXT_BYTES = MAX_WIRE_BYTES - HEADER_LENGTH - NONCE_LENGTH - GCM_TAG_LENGTH;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final byte[] HKDF_SALT = "ProAntiTab/plugin-messaging/v1".getBytes(StandardCharsets.UTF_8);

    // Stable packet IDs. Do not reorder/reuse IDs without a protocol-version bump.
    private static final byte P_UPDATE = 1;
    private static final byte P_IDENTITY = 2;
    private static final byte P_NOTIFICATION = 3;
    private static final byte P_EXEC_CONSOLE = 4;
    private static final byte P_EXEC_PLAYER = 5;
    private static final byte P_CONSOLE_MESSAGE = 6;
    private static final byte P_KEEPALIVE_RESPONSE = 7;
    private static final byte P_DATA_SYNC = 8;
    private static final byte P_IDENTITY_REQUEST = 20;
    private static final byte P_IDENTITY_RESPONSE = 21;
    private static final byte P_DATA_SYNC_RECEIVED = 22;
    private static final byte P_KEEPALIVE = 23;
    private static final byte P_PLAYER_PERMISSION_CHANGE = 24;
    private static final byte P_GROUP_PERMISSION_CHANGE = 25;

    public static byte[] preparePacket(Object object) {
        return preparePacket(object, Communicator.get().getId());
    }

    public static byte[] preparePacket(Object object, UUID id) {
        if (!isValidPacket(object) || Storage.TOKEN == null || Storage.TOKEN.isBlank()) {
            return null;
        }

        try {
            final boolean initial = isInitialPacket(object);
            if (!initial && id == null) {
                return null;
            }
            final byte[] plaintext = encodePacket((PATPacket) object);
            final byte[] nonce = new byte[NONCE_LENGTH];
            RANDOM.nextBytes(nonce);

            // Wire format: [version:1][init:1][nonce:12][ciphertext + GCM tag:16].
            // The version and init flag are authenticated as GCM additional authenticated data.
            final byte[] header = new byte[]{PROTOCOL_VERSION, initial ? INIT_PACKET : NOT_INIT_PACKET};
            final SecretKeySpec key = deriveKey(Storage.TOKEN, initial ? null : id);
            final byte[] ciphertext = encrypt(plaintext, key, nonce, header);

            final byte[] result = new byte[header.length + NONCE_LENGTH + ciphertext.length];
            System.arraycopy(header, 0, result, 0, header.length);
            System.arraycopy(nonce, 0, result, header.length, NONCE_LENGTH);
            System.arraycopy(ciphertext, 0, result, header.length + NONCE_LENGTH, ciphertext.length);
            return result;
        } catch (IOException | GeneralSecurityException ignored) {
            return null;
        }
    }

    public static Object readPacket(byte[] bytes) {
        return readPacket(bytes, Communicator.get().getId());
    }

    public static Object readPacket(byte[] bytes, UUID id) {
        if (bytes == null || bytes.length < HEADER_LENGTH + NONCE_LENGTH + GCM_TAG_LENGTH
                || bytes.length > MAX_WIRE_BYTES || Storage.TOKEN == null || Storage.TOKEN.isBlank()) {
            return null;
        }

        try {
            final byte version = bytes[0];
            final byte initFlag = bytes[1];
            if (version != PROTOCOL_VERSION || (initFlag != INIT_PACKET && initFlag != NOT_INIT_PACKET)) {
                return null;
            }

            final boolean initial = initFlag == INIT_PACKET;
            if (!initial && id == null) {
                return null;
            }

            final byte[] header = Arrays.copyOfRange(bytes, 0, 2);
            final byte[] nonce = Arrays.copyOfRange(bytes, 2, 2 + NONCE_LENGTH);
            final byte[] ciphertext = Arrays.copyOfRange(bytes, 2 + NONCE_LENGTH, bytes.length);

            final SecretKeySpec key = deriveKey(Storage.TOKEN, initial ? null : id);
            // Authentication is verified by GCM before decodePacket() sees any plaintext.
            final byte[] plaintext = decrypt(ciphertext, key, nonce, header);
            return decodePacket(plaintext, initial);
        } catch (IOException | GeneralSecurityException ignored) {
            return null;
        }
    }

    private static SecretKeySpec deriveKey(String token, UUID id) throws GeneralSecurityException {
        // RFC 5869 HKDF-SHA-256. The token is the input keying material; the UUID is
        // public context used to separate normal-session keys between backend identities.
        final Mac mac = Mac.getInstance("HmacSHA256");

        // HKDF-Extract(salt, IKM)
        mac.init(new SecretKeySpec(HKDF_SALT, "HmacSHA256"));
        final byte[] prk = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));

        // HKDF-Expand(PRK, info, 32). One HMAC block is sufficient for an AES-256 key.
        final String context = id == null ? "initial" : "backend:" + id;
        final byte[] info = context.getBytes(StandardCharsets.UTF_8);
        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        mac.update(info);
        mac.update((byte) 0x01);
        final byte[] okm = mac.doFinal();
        return new SecretKeySpec(okm, "AES");
    }

    private static byte[] encrypt(byte[] plaintext, SecretKeySpec key, byte[] nonce, byte[] aad) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, nonce));
        cipher.updateAAD(aad);
        return cipher.doFinal(plaintext);
    }

    private static byte[] decrypt(byte[] ciphertext, SecretKeySpec key, byte[] nonce, byte[] aad) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, nonce));
        cipher.updateAAD(aad);
        return cipher.doFinal(ciphertext);
    }

    private static byte[] encodePacket(PATPacket packet) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new BoundedOutputStream(buffer, MAX_PLAINTEXT_BYTES))) {
            if (packet instanceof Proxy2Backend.UpdatePacket p) {
                out.writeByte(P_UPDATE); writeNullableUuid(out, p.playerId());
            } else if (packet instanceof Proxy2Backend.IdentityPacket p) {
                out.writeByte(P_IDENTITY); writeUuid(out, p.targetServerId()); writeString(out, p.serverName());
            } else if (packet instanceof Proxy2Backend.NotificationPacket p) {
                out.writeByte(P_NOTIFICATION); writeUuid(out, p.playerId()); writeString(out, p.command());
            } else if (packet instanceof Proxy2Backend.ExecuteConsoleCommandPacket p) {
                out.writeByte(P_EXEC_CONSOLE); writeString(out, p.command());
            } else if (packet instanceof Proxy2Backend.ExecutePlayerCommandPacket p) {
                out.writeByte(P_EXEC_PLAYER); writeUuid(out, p.playerId()); writeString(out, p.command());
            } else if (packet instanceof Proxy2Backend.ConsoleMessagePacket p) {
                out.writeByte(P_CONSOLE_MESSAGE); writeString(out, p.message());
            } else if (packet instanceof Proxy2Backend.KeepAliveResponsePacket) {
                out.writeByte(P_KEEPALIVE_RESPONSE);
            } else if (packet instanceof Proxy2Backend.DataSyncPacket p) {
                out.writeByte(P_DATA_SYNC);
                writeString(out, p.messages().prefix());
                out.writeBoolean(p.autoLowerCase().enabled());
                out.writeBoolean(p.unknownCommand().enabled());
                final List<String> lines = p.unknownCommand().message().getLines();
                out.writeInt(lines.size());
                for (String line : lines) writeString(out, line);
            } else if (packet instanceof Backend2Proxy.IdentityRequestPacket p) {
                out.writeByte(P_IDENTITY_REQUEST); writeUuid(out, p.serverId());
            } else if (packet instanceof Backend2Proxy.IdentityResponsePacket p) {
                out.writeByte(P_IDENTITY_RESPONSE); writeUuid(out, p.serverId());
            } else if (packet instanceof Backend2Proxy.DataSyncReceivedPacket) {
                out.writeByte(P_DATA_SYNC_RECEIVED);
            } else if (packet instanceof Backend2Proxy.KeepAlivePacket) {
                out.writeByte(P_KEEPALIVE);
            } else if (packet instanceof Backend2Proxy.AnnouncePlayerPermissionChanges p) {
                out.writeByte(P_PLAYER_PERMISSION_CHANGE); writeUuid(out, p.playerId());
            } else if (packet instanceof Backend2Proxy.AnnounceGroupPermissionChanges p) {
                out.writeByte(P_GROUP_PERMISSION_CHANGE);
                out.writeInt(p.playerIds().size());
                for (UUID uuid : p.playerIds()) writeUuid(out, uuid);
            } else {
                throw new IOException("Unknown packet type");
            }
        }
        if (buffer.size() > MAX_PLAINTEXT_BYTES) throw new IOException("Packet too large");
        return buffer.toByteArray();
    }

    private static PATPacket decodePacket(byte[] bytes, boolean initial) throws IOException {
        if (bytes.length == 0 || bytes.length > MAX_PLAINTEXT_BYTES) throw new IOException("Invalid packet length");
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            final int type = in.readUnsignedByte();
            final PATPacket packet = switch (type) {
                case P_UPDATE -> new Proxy2Backend.UpdatePacket(readNullableUuid(in));
                case P_IDENTITY -> new Proxy2Backend.IdentityPacket(readUuid(in), readString(in));
                case P_NOTIFICATION -> new Proxy2Backend.NotificationPacket(readUuid(in), readString(in));
                case P_EXEC_CONSOLE -> new Proxy2Backend.ExecuteConsoleCommandPacket(readString(in));
                case P_EXEC_PLAYER -> new Proxy2Backend.ExecutePlayerCommandPacket(readUuid(in), readString(in));
                case P_CONSOLE_MESSAGE -> new Proxy2Backend.ConsoleMessagePacket(readString(in));
                case P_KEEPALIVE_RESPONSE -> new Proxy2Backend.KeepAliveResponsePacket();
                case P_DATA_SYNC -> {
                    final Proxy2Backend.DataSyncPacket.Messages messages =
                            new Proxy2Backend.DataSyncPacket.Messages(readString(in));
                    final Proxy2Backend.DataSyncPacket.AutoLowerCase autoLowerCase =
                            new Proxy2Backend.DataSyncPacket.AutoLowerCase(in.readBoolean());
                    final boolean unknownEnabled = in.readBoolean();
                    final int count = readCount(in, Integer.BYTES);
                    final List<String> lines = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) lines.add(readString(in));
                    final Proxy2Backend.DataSyncPacket.UnknownCommand unknown =
                            new Proxy2Backend.DataSyncPacket.UnknownCommand(
                                    unknownEnabled, new MultipleMessagesHelper(lines));
                    yield new Proxy2Backend.DataSyncPacket(messages, autoLowerCase, unknown);
                }
                case P_IDENTITY_REQUEST -> new Backend2Proxy.IdentityRequestPacket(readUuid(in));
                case P_IDENTITY_RESPONSE -> new Backend2Proxy.IdentityResponsePacket(readUuid(in));
                case P_DATA_SYNC_RECEIVED -> new Backend2Proxy.DataSyncReceivedPacket();
                case P_KEEPALIVE -> new Backend2Proxy.KeepAlivePacket();
                case P_PLAYER_PERMISSION_CHANGE -> new Backend2Proxy.AnnouncePlayerPermissionChanges(readUuid(in));
                case P_GROUP_PERMISSION_CHANGE -> {
                    final int count = readCount(in, 2 * Long.BYTES);
                    final Set<UUID> ids = new HashSet<>(Math.max(16, count));
                    for (int i = 0; i < count; i++) ids.add(readUuid(in));
                    yield new Backend2Proxy.AnnounceGroupPermissionChanges(ids);
                }
                default -> throw new IOException("Unknown packet type: " + type);
            };

            if (in.available() != 0) throw new IOException("Trailing packet data");
            if (initial != isInitialPacket(packet)) throw new IOException("Packet/header type mismatch");
            return packet;
        }
    }

    // Reject oversized plaintext as it is written, rather than after allocating it.
    private static final class BoundedOutputStream extends FilterOutputStream {
        private final int limit;
        private int written;

        private BoundedOutputStream(OutputStream out, int limit) {
            super(out);
            this.limit = limit;
        }

        @Override
        public void write(int value) throws IOException {
            if (written >= limit) throw new IOException("Packet too large");
            out.write(value);
            written++;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            if (length > limit - written) throw new IOException("Packet too large");
            out.write(bytes, offset, length);
            written += length;
        }
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        if (value == null) throw new IOException("Null string");
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        final int length = in.readInt();
        if (length < 0 || length > in.available()) {
            throw new IOException("Invalid string length");
        }
        return new String(in.readNBytes(length), StandardCharsets.UTF_8);
    }

    private static int readCount(DataInputStream in, int minimumEntryBytes) throws IOException {
        final int count = in.readInt();
        if (count < 0 || count > in.available() / minimumEntryBytes) {
            throw new IOException("Invalid collection size");
        }
        return count;
    }

    private static void writeUuid(DataOutputStream out, UUID uuid) throws IOException {
        if (uuid == null) throw new IOException("Null UUID");
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    private static UUID readUuid(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }

    private static void writeNullableUuid(DataOutputStream out, UUID uuid) throws IOException {
        out.writeBoolean(uuid != null);
        if (uuid != null) writeUuid(out, uuid);
    }

    private static UUID readNullableUuid(DataInputStream in) throws IOException {
        return in.readBoolean() ? readUuid(in) : null;
    }

    public static boolean isValidPacket(Object object) { return object instanceof PATPacket; }
    public static boolean isP2BPacket(Object object) { return object instanceof P2BPacket; }
    public static boolean isB2PPacket(Object object) { return object instanceof B2PPacket; }
    public static boolean isInitialPacket(Object object) { return object instanceof InitialPacket; }
    // Routing hint only; the flag is authenticated by readPacket().
    public static boolean isInitialPacket(byte[] bytes) { return bytes != null && bytes.length > 1 && bytes[0] == PROTOCOL_VERSION && bytes[1] == INIT_PACKET; }

    public interface PATPacket { }
    private interface InitialPacket extends PATPacket { }
    private interface Synchronizable { }
    private interface P2BPacket extends PATPacket { }
    private interface B2PPacket extends PATPacket { }

    public static class Proxy2Backend {
        private Proxy2Backend() { }
        public record UpdatePacket(UUID playerId) implements P2BPacket { public boolean forEveryone() { return playerId == null; } }
        public record IdentityPacket(UUID targetServerId, String serverName) implements InitialPacket, P2BPacket { }
        public record NotificationPacket(UUID playerId, String command) implements P2BPacket { }
        public record ExecuteConsoleCommandPacket(String command) implements P2BPacket { }
        public record ExecutePlayerCommandPacket(UUID playerId, String command) implements P2BPacket { }
        public record ConsoleMessagePacket(String message) implements P2BPacket { }
        public record KeepAliveResponsePacket() implements P2BPacket { }
        public record DataSyncPacket(Messages messages, AutoLowerCase autoLowerCase, UnknownCommand unknownCommand) implements P2BPacket {
            public record Messages(String prefix) implements Synchronizable { }
            public record AutoLowerCase(boolean enabled) implements Synchronizable { }
            public record UnknownCommand(boolean enabled, MultipleMessagesHelper message) implements Synchronizable { }
        }
    }

    public static class Backend2Proxy {
        private Backend2Proxy() { }
        public record IdentityRequestPacket(UUID serverId) implements InitialPacket, B2PPacket { }
        public record IdentityResponsePacket(UUID serverId) implements InitialPacket, B2PPacket { }
        public record DataSyncReceivedPacket() implements B2PPacket { }
        public record KeepAlivePacket() implements B2PPacket { }
        public record AnnouncePlayerPermissionChanges(UUID playerId) implements B2PPacket { }
        public record AnnounceGroupPermissionChanges(Set<UUID> playerIds) implements B2PPacket { }
    }
}
