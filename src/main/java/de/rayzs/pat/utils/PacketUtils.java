package de.rayzs.pat.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class PacketUtils {

    /*
        Copyright (c) 2012, md_5. All rights reserved.
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java#L72-L94
     */

    public static String readString(ByteBuf buf) throws Exception {
        return readString(buf, Short.MAX_VALUE);
    }

    public static String readString(ByteBuf byteBuf, int maxLength) throws Exception {
        int len = readVarInt(byteBuf);
        if (len > maxLength * 3)
            throw new Exception("Cannot receive string longer than " + maxLength * 3 + " (got " + len + " bytes)");

        String string = byteBuf.toString(byteBuf.readerIndex(), len, StandardCharsets.UTF_8);
        byteBuf.readerIndex(byteBuf.readerIndex() + len);

        if (string.length() > maxLength)
            throw new Exception("Cannot receive string longer than " + maxLength + " (got " + string.length() + " characters)");

        return string;
    }

    /*
        Copyright (c) 2012, md_5. All rights reserved.
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java#L233-L261
     */

    public static int readVarInt(ByteBuf input) throws Exception {
        return readVarInt(input, 5);
    }

    public static int readVarInt(ByteBuf input, int maxBytes) throws Exception {
        int out = 0, bytes = 0;
        byte in;

        while (true) {
            in = input.readByte();
            out |= (in & 0x7F) << (bytes++ * 7);
            if (bytes > maxBytes) throw new Exception("VarInt too big (max " + maxBytes + ")");
            if ((in & 0x80) != 0x80) break;
        }

        return out;
    }

   /*
        Copyright (c) 2012, md_5. All rights reserved.
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java#L263C1-L283C6
     */

    public static void writeVarInt(int value, ByteBuf output) {
        int part;
        while (true) {
            part = value & 0x7F;
            value >>>= 7;

            if (value != 0) part |= 0x80;
            output.writeByte(part);

            if (value == 0) break;
        }
    }

    /*
        Copyright (c) 2012, md_5. All rights reserved.
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java#L50C4-L71C1
     */

    public static void writeString(String string, ByteBuf byteBuf) throws Exception {
        writeString(string, byteBuf, Short.MAX_VALUE);
    }

    public static void writeString(String string, ByteBuf byteBuf, int maxLength) throws Exception {
        if (string.length() > maxLength)
            throw new Exception("Cannot send string longer than " + maxLength + " (got " + string.length() + " characters)");

        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > maxLength * 3)
            throw new Exception("Cannot send string longer than " + (maxLength * 3) + " (got " + bytes.length + " bytes)");

        writeVarInt(bytes.length, byteBuf);
        byteBuf.writeBytes(bytes);
    }

    public static class BrandManipulate {

        private final int capacity;
        private final String brand;
        private final byte[] bytes;
        private ByteBuf byteBuf;

        public BrandManipulate(String brand) {
            this.brand = brand;
            this.capacity = brand.getBytes(StandardCharsets.UTF_8).length + 1;
            bytes = buildBytes(true);
        }

        public BrandManipulate(String brand, boolean releases) {
            this.brand = brand;
            this.capacity = brand.getBytes(StandardCharsets.UTF_8).length + 1;
            bytes = buildBytes(releases);
        }

        public byte[] getBytes() {
            return bytes;
        }

        private byte[] buildBytes(boolean release) {
            byteBuf = Unpooled.buffer(this.capacity);

            try {
                writeString(brand, byteBuf);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            byte[] bytes = byteBuf.array();
            if (release) byteBuf.release();

            return bytes;
        }

        public String getBrand() {
            return brand;
        }

        public ByteBuf getByteBuf() {
            return byteBuf;
        }
    }
}
