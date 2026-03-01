package de.rayzs.pat.plugin.system.serverbrand.impl;

import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.plugin.system.serverbrand.ServerBrand;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.protocol.ProtocolConstants;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.storage.Storage;
import com.velocitypowered.api.proxy.*;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Method;
import io.netty.buffer.ByteBuf;
import de.rayzs.pat.utils.*;
import java.util.Optional;

public class VelocityServerBrand implements ServerBrand {

    private static final ProxyServer SERVER = VelocityLoader.getServer();
    private static Class<?> pluginMessagePacketClass, minecraftConnectionClass, connectedPlayerConnectionClass;
    private static Method connectionMethod;
    private static ScheduledTask TASK;

    private static String BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0);

    @Override
    public void initializeTask() {
        if (TASK != null) {
            TASK.cancel();
        }

        if (!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return;
        }

        if (pluginMessagePacketClass == null) {
            pluginMessagePacketClass = Reflection.getClass("com.velocitypowered.proxy.protocol.packet.PluginMessagePacket");
        }

        if (minecraftConnectionClass == null) {
            minecraftConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.MinecraftConnection");
        }

        if (connectedPlayerConnectionClass == null) {
            connectedPlayerConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.client.ConnectedPlayer");
        }

        if (connectionMethod == null) {
            connectionMethod = Reflection.getMethodByName(connectedPlayerConnectionClass, "getConnection");
        }

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            BRAND = MessageTranslator.replaceMessage(Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0)) + "§r";
            SERVER.getAllPlayers().forEach(this::send);
        } else {
            TASK = SERVER.getScheduler().buildTask(VelocityLoader.getInstance(), new Runnable() {

                final int length = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().size();
                int animationPos = 0;

                @Override
                public void run() {
                    animationPos = (++animationPos) % length;

                    BRAND = MessageTranslator.replaceMessage(
                            Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(animationPos)
                    ) + "§r";

                    SERVER.getAllPlayers().forEach(player -> send(player));
                }

            }).repeat(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY, TimeUnit.MILLISECONDS).schedule();
        }
    }


    @Override
    public void preparePlayer(Object playerObj) { }

    @Override
    public void send(Object playerObj) {
        if (!(playerObj instanceof Player player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return;

        try {
            final Object connectedPlayerObj = connectedPlayerConnectionClass.cast(player);
            final Object minecraftConnectionObj = connectionMethod.invoke(connectedPlayerObj);

            final PacketUtils.BrandManipulate serverBrand = createPacket(playerObj);
            final String brand = player.getProtocolVersion().getProtocol() >= ProtocolConstants.MINECRAFT_1_13
                    ? "minecraft:brand" : "MC|Brand";


            final Object pluginMessagePacket = pluginMessagePacketClass
                    .getConstructor(String.class, ByteBuf.class)
                    .newInstance(brand, serverBrand.getByteBuf());


            Reflection.getMethodsByParameterAndName(minecraftConnectionObj, "write", Object.class)
                    .get(0).invoke(minecraftConnectionObj, pluginMessagePacket);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public PacketUtils.BrandManipulate createPacket(Object playerObj) {
        if (!(playerObj instanceof Player player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return null;

        try {
            final Optional<ServerConnection> serverConnection = player.getCurrentServer();


            final String serverName = serverConnection.isPresent()
                    ? serverConnection.get().getServerInfo().getName() : "";

            final String playerName = player.getUsername();
            final String ping = String.valueOf(player.getPing());

            final String customBrand = StringUtils.replace(BRAND,
                    "%player%", playerName,
                    "%server%", serverName,
                    "%ping%", ping
            );

            return new PacketUtils.BrandManipulate(customBrand, false);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }
}
