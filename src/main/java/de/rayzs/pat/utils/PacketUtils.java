package de.rayzs.pat.utils;

import java.nio.charset.StandardCharsets;
import io.netty.buffer.*;

public class PacketUtils {

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

            if ( value != 0 ) part |= 0x80;
            output.writeByte(part);

            if ( value == 0 ) break;
        }
    }

    /*
        Copyright (c) 2012, md_5. All rights reserved.
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java#L50C4-L71C1
     */

    public static void writeString(String string, ByteBuf byteBuf) throws Exception {
        writeString(string, byteBuf, Short.MAX_VALUE );
    }

    public static void writeString(String string, ByteBuf byteBuf, int maxLength) throws Exception {
        if (string.length() > maxLength )
            throw new Exception( "Cannot send string longer than " + maxLength + " (got " + string.length() + " characters)");

        byte[] bytes = string.getBytes( StandardCharsets.UTF_8 );
        if (bytes.length > maxLength * 3)
            throw new Exception( "Cannot send string longer than " + ( maxLength * 3 ) + " (got " + bytes.length + " bytes)");

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
            if(release) byteBuf.release();

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
