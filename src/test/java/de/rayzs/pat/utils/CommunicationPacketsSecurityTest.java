package de.rayzs.pat.utils;

import de.rayzs.pat.api.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationPacketsSecurityTest {

    private static final String TOKEN = "test-token-6d21af0b-e44e-4c94-9920-7ecbd96243e1";
    private static final UUID BACKEND_ID = UUID.fromString("28ef48c0-62cc-4f25-b2f0-6fb53d2dd61b");
    private static final UUID OTHER_BACKEND_ID = UUID.fromString("dfe05064-55c9-4b3b-a65f-d6beaeb0b9af");
    private static final String EXPECTED_HKDF_SALT = "ProAntiTab/plugin-messaging/v1";

    private String previousToken;

    @BeforeEach
    void setUp() {
        previousToken = Storage.TOKEN;
        Storage.TOKEN = TOKEN;
    }

    @AfterEach
    void tearDown() {
        Storage.TOKEN = previousToken;
    }

    @Test
    void normalPacketRoundTripsWithMatchingBackendContext() {
        UUID playerId = UUID.fromString("7c9f9835-cf72-49c1-aaf0-e4c9853cf7fa");
        var original = new CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket(
                playerId, "warp spawn"
        );

        byte[] encoded = CommunicationPackets.preparePacket(original, BACKEND_ID);
        Object decoded = CommunicationPackets.readPacket(encoded, BACKEND_ID);

        assertNotNull(encoded);
        assertEquals(original, decoded);
        assertTrue(CommunicationPackets.isP2BPacket(decoded));
        assertFalse(CommunicationPackets.isInitialPacket(decoded));
    }

    @Test
    void initialHandshakePacketRoundTripsIndependentlyOfBackendContext() {
        var original = new CommunicationPackets.Backend2Proxy.IdentityRequestPacket(BACKEND_ID);

        byte[] encoded = CommunicationPackets.preparePacket(original, BACKEND_ID);

        assertNotNull(encoded);
        assertTrue(CommunicationPackets.isInitialPacket(encoded));
        assertEquals(original, CommunicationPackets.readPacket(encoded, OTHER_BACKEND_ID));
    }

    @Test
    void samePlaintextProducesDifferentCiphertextBecauseNonceIsRandom() {
        var packet = new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket("same message");

        byte[] first = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        byte[] second = CommunicationPackets.preparePacket(packet, BACKEND_ID);

        assertNotNull(first);
        assertNotNull(second);
        assertFalse(Arrays.equals(first, second), "AES-GCM packets must use fresh nonces");
        assertEquals(packet, CommunicationPackets.readPacket(first, BACKEND_ID));
        assertEquals(packet, CommunicationPackets.readPacket(second, BACKEND_ID));
    }

    @Test
    void tamperingWithCiphertextIsRejected() {
        var packet = new CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket("say secure");
        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(encoded);

        byte[] tampered = encoded.clone();
        tampered[tampered.length - 1] ^= 0x01;

        assertNull(CommunicationPackets.readPacket(tampered, BACKEND_ID));
    }

    @Test
    void tamperingWithAuthenticatedHeaderIsRejected() {
        var packet = new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket();
        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(encoded);

        byte[] tampered = encoded.clone();
        tampered[1] = 0; // claim this normal packet is an initial packet

        assertNull(CommunicationPackets.readPacket(tampered, BACKEND_ID));
    }

    @Test
    void wrongBackendUuidCannotDecryptNormalPacket() {
        var packet = new CommunicationPackets.Backend2Proxy.AnnouncePlayerPermissionChanges(UUID.randomUUID());
        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);

        assertNotNull(encoded);
        assertNull(CommunicationPackets.readPacket(encoded, OTHER_BACKEND_ID));
        assertEquals(packet, CommunicationPackets.readPacket(encoded, BACKEND_ID));
    }

    @Test
    void wrongStorageTokenCannotDecryptPacket() {
        var packet = new CommunicationPackets.Proxy2Backend.NotificationPacket(
                UUID.randomUUID(), "help"
        );
        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(encoded);

        Storage.TOKEN = "different-token-that-must-not-authenticate";
        assertNull(CommunicationPackets.readPacket(encoded, BACKEND_ID));
    }

    @Test
    void truncatedAndInvalidFramesAreRejectedWithoutThrowing() {
        assertDoesNotThrow(() -> {
            assertNull(CommunicationPackets.readPacket(null, BACKEND_ID));
            assertNull(CommunicationPackets.readPacket(new byte[0], BACKEND_ID));
            assertNull(CommunicationPackets.readPacket(new byte[8], BACKEND_ID));
        });

        var packet = new CommunicationPackets.Backend2Proxy.KeepAlivePacket();
        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(encoded);

        for (int length : new int[]{1, 2, 5, 13, encoded.length - 1}) {
            byte[] truncated = Arrays.copyOf(encoded, length);
            assertDoesNotThrow(() -> assertNull(CommunicationPackets.readPacket(truncated, BACKEND_ID)));
        }
    }

    @Test
    void invalidProtocolVersionIsRejected() {
        var packet = new CommunicationPackets.Backend2Proxy.KeepAlivePacket();
        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(encoded);

        byte[] wrongVersion = encoded.clone();
        wrongVersion[0] = (byte) (CommunicationPackets.PROTOCOL_VERSION + 1);

        assertNull(CommunicationPackets.readPacket(wrongVersion, BACKEND_ID));
    }

    @Test
    void blankTokenDisablesEncodingAndDecoding() {
        var packet = new CommunicationPackets.Backend2Proxy.KeepAlivePacket();
        byte[] valid = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(valid);

        Storage.TOKEN = "   ";
        assertNull(CommunicationPackets.preparePacket(packet, BACKEND_ID));
        assertNull(CommunicationPackets.readPacket(valid, BACKEND_ID));
    }

    @Test
    void representativePacketTypesPreserveTheirPayloads() {
        UUID player = UUID.fromString("c112a6b7-82ec-4120-a4f6-f7c6d0953eef");
        Set<UUID> players = new HashSet<>(Set.of(
                player,
                UUID.fromString("f59bad95-cd4c-4fc2-a76b-834f38de2447")
        ));

        CommunicationPackets.PATPacket[] packets = {
                new CommunicationPackets.Proxy2Backend.UpdatePacket(null),
                new CommunicationPackets.Proxy2Backend.UpdatePacket(player),
                new CommunicationPackets.Proxy2Backend.IdentityPacket(BACKEND_ID, "survival"),
                new CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket("list"),
                new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket("hello"),
                new CommunicationPackets.Backend2Proxy.IdentityResponsePacket(BACKEND_ID),
                new CommunicationPackets.Backend2Proxy.DataSyncReceivedPacket(),
                new CommunicationPackets.Backend2Proxy.AnnounceGroupPermissionChanges(players)
        };

        for (CommunicationPackets.PATPacket packet : packets) {
            byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
            assertNotNull(encoded, () -> "Failed to encode " + packet.getClass().getSimpleName());
            assertEquals(packet, CommunicationPackets.readPacket(encoded, BACKEND_ID),
                    () -> "Round-trip mismatch for " + packet.getClass().getSimpleName());
        }
    }

    @Test
    void normalPacketRequiresBackendContext() {
        var packet = new CommunicationPackets.Backend2Proxy.KeepAlivePacket();

        assertNull(CommunicationPackets.preparePacket(packet, null));

        byte[] encoded = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(encoded);
        assertNull(CommunicationPackets.readPacket(encoded, null));
    }

    private static CommunicationPackets.Proxy2Backend.DataSyncPacket sync(String prefix, List<String> lines) {
        return new CommunicationPackets.Proxy2Backend.DataSyncPacket(
                new CommunicationPackets.Proxy2Backend.DataSyncPacket.Messages(prefix),
                new CommunicationPackets.Proxy2Backend.DataSyncPacket.AutoLowerCase(true),
                new CommunicationPackets.Proxy2Backend.DataSyncPacket.UnknownCommand(true, new MultipleMessagesHelper(lines)));
    }

    private static void assertRoundTrip(CommunicationPackets.PATPacket packet) {
        byte[] wire = CommunicationPackets.preparePacket(packet, BACKEND_ID);
        assertNotNull(wire, () -> "Encoding failed for " + packet.getClass().getSimpleName());
        Object decoded = CommunicationPackets.readPacket(wire, BACKEND_ID);
        if (packet instanceof CommunicationPackets.Proxy2Backend.DataSyncPacket original) {
            var actual = assertInstanceOf(CommunicationPackets.Proxy2Backend.DataSyncPacket.class, decoded);
            assertEquals(original.messages(), actual.messages());
            assertEquals(original.autoLowerCase(), actual.autoLowerCase());
            assertEquals(original.unknownCommand().enabled(), actual.unknownCommand().enabled());
            assertEquals(original.unknownCommand().message().getLines(), actual.unknownCommand().message().getLines());
        } else {
            assertEquals(packet, decoded);
        }
    }

    @Test
    void allFourteenPacketTypesRoundTrip() {
        UUID player = UUID.fromString("f5b80b7d-c1ad-48f0-b07b-4bd3e24fb8f2");
        CommunicationPackets.PATPacket[] packets = {
                new CommunicationPackets.Proxy2Backend.UpdatePacket(null),
                new CommunicationPackets.Proxy2Backend.IdentityPacket(BACKEND_ID, "survival"),
                new CommunicationPackets.Proxy2Backend.NotificationPacket(player, "help"),
                new CommunicationPackets.Proxy2Backend.ExecuteConsoleCommandPacket("list"),
                new CommunicationPackets.Proxy2Backend.ExecutePlayerCommandPacket(player, "spawn"),
                new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket("hello"),
                new CommunicationPackets.Proxy2Backend.KeepAliveResponsePacket(),
                sync("prefix", List.of("one", "two", "three")),
                new CommunicationPackets.Backend2Proxy.IdentityRequestPacket(BACKEND_ID),
                new CommunicationPackets.Backend2Proxy.IdentityResponsePacket(BACKEND_ID),
                new CommunicationPackets.Backend2Proxy.DataSyncReceivedPacket(),
                new CommunicationPackets.Backend2Proxy.KeepAlivePacket(),
                new CommunicationPackets.Backend2Proxy.AnnouncePlayerPermissionChanges(player),
                new CommunicationPackets.Backend2Proxy.AnnounceGroupPermissionChanges(Set.of(player, BACKEND_ID))
        };
        assertEquals(14, packets.length);
        for (var packet : packets) assertRoundTrip(packet);
    }

    @Test
    void maximumPlaintextAndWireSizeAreAcceptedAndNextByteRejected() {
        // 19 bytes of framing + 16,384 + 8,192 + 8,142 bytes of strings = 32,737 bytes.
        var maximum = sync("a".repeat(16384), List.of("b".repeat(8192), "c".repeat(8142)));
        byte[] wire = CommunicationPackets.preparePacket(maximum, BACKEND_ID);
        assertNotNull(wire);
        assertEquals(32_767, wire.length);
        assertRoundTrip(maximum);
        assertNull(CommunicationPackets.readPacket(Arrays.copyOf(wire, wire.length + 1), BACKEND_ID));

        var overMaximum = sync("a".repeat(16384), List.of("b".repeat(8192), "c".repeat(8143)));
        assertNull(CommunicationPackets.preparePacket(overMaximum, BACKEND_ID));
    }

    @Test
    void utf8StringsAreLimitedByTotalPacketBytes() {
        // 1-byte type + 4-byte length + UTF-8 bytes.
        assertRoundTrip(new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket("é".repeat(16_366)));
        assertNull(CommunicationPackets.preparePacket(
                new CommunicationPackets.Proxy2Backend.ConsoleMessagePacket("é".repeat(16_367)), BACKEND_ID));
    }

    @Test
    void collectionsAreLimitedByTotalPacketBytes() {
        // Each empty string still takes 4 bytes for its length prefix.
        assertRoundTrip(sync("", java.util.Collections.nCopies(8_181, "")));
        assertNull(CommunicationPackets.preparePacket(
                sync("", java.util.Collections.nCopies(8_182, "")), BACKEND_ID));

        Set<UUID> ids = new HashSet<>();
        for (int i = 0; i < 2_045; i++) ids.add(new UUID(0, i));
        assertRoundTrip(new CommunicationPackets.Backend2Proxy.AnnounceGroupPermissionChanges(ids));
        ids.add(new UUID(0, 2_045));
        assertNull(CommunicationPackets.preparePacket(
                new CommunicationPackets.Backend2Proxy.AnnounceGroupPermissionChanges(ids), BACKEND_ID));
    }

    // Independently encrypt arbitrary plaintext to exercise malformed authenticated codec input.
    private static byte[] authenticatedFrame(byte[] plaintext, boolean initial) throws Exception {
        Method derive = CommunicationPackets.class.getDeclaredMethod("deriveKey", String.class, UUID.class);
        derive.setAccessible(true);
        SecretKeySpec key = (SecretKeySpec) derive.invoke(null, TOKEN, initial ? null : BACKEND_ID);
        byte[] header = {CommunicationPackets.PROTOCOL_VERSION, (byte) (initial ? 0 : 1)};
        byte[] nonce = new byte[12]; // Deterministic nonce used only by isolated test frames.
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        cipher.updateAAD(header);
        byte[] ciphertext = cipher.doFinal(plaintext);
        byte[] frame = new byte[header.length + nonce.length + ciphertext.length];
        System.arraycopy(header, 0, frame, 0, 2);
        System.arraycopy(nonce, 0, frame, 2, 12);
        System.arraycopy(ciphertext, 0, frame, 14, ciphertext.length);
        return frame;
    }

    private static byte[] plaintext(int type, int length) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeByte(type);
        out.writeInt(length);
        return bytes.toByteArray();
    }

    @Test
    void authenticatedMalformedPayloadsAreRejected() throws Exception {
        // Unknown packet type, trailing bytes, invalid/negative string lengths.
        byte[][] invalid = {
                {(byte) 127},
                {23, 1},
                plaintext(6, -1),
                plaintext(6, 32_738),
                plaintext(6, 10),
                plaintext(25, -1),
                plaintext(25, Integer.MAX_VALUE),
                plaintext(8, -1),
                plaintext(8, Integer.MAX_VALUE)
        };
        for (byte[] payload : invalid) {
            assertNull(CommunicationPackets.readPacket(authenticatedFrame(payload, false), BACKEND_ID),
                    "Malformed authenticated packet accepted: " + Arrays.toString(payload));
        }
        // Initial flag must match decoded packet type, even with a valid GCM tag.
        assertNull(CommunicationPackets.readPacket(authenticatedFrame(new byte[]{23}, true), BACKEND_ID));
        assertNull(CommunicationPackets.readPacket(authenticatedFrame(new byte[]{20}, false), BACKEND_ID));
    }

    @Test
    void hkdfMatchesIndependentExtractExpandComputation() throws Exception {
        Method derive = CommunicationPackets.class.getDeclaredMethod("deriveKey", String.class, UUID.class);
        derive.setAccessible(true);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(EXPECTED_HKDF_SALT.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] prk = mac.doFinal(TOKEN.getBytes(StandardCharsets.UTF_8));
        for (UUID context : new UUID[]{null, BACKEND_ID}) {
            String info = context == null ? "initial" : "backend:" + context;
            mac.init(new SecretKeySpec(prk, "HmacSHA256"));
            mac.update(info.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 1);
            byte[] expected = mac.doFinal();
            SecretKeySpec actual = (SecretKeySpec) derive.invoke(null, TOKEN, context);
            assertArrayEquals(expected, actual.getEncoded());
        }
    }


    @Test
    void oversizedWirePacketIsRejectedBeforeDecryption() {
        // Default Velocity maximum payload size plus one byte.
        byte[] oversized = new byte[32_768];
        oversized[0] = CommunicationPackets.PROTOCOL_VERSION;
        oversized[1] = 1;

        assertNull(CommunicationPackets.readPacket(oversized, BACKEND_ID));
    }
}
