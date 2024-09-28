package de.rayzs.pat.api.brand.impl;

import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.protocol.ProtocolConstants;
import java.util.concurrent.atomic.AtomicInteger;
import de.rayzs.pat.api.brand.ServerBrand;
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
        if(TASK != null) TASK.cancel();

        if(!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return;

        if(pluginMessagePacketClass == null)
            pluginMessagePacketClass = Reflection.getClass("com.velocitypowered.proxy.protocol.packet.PluginMessagePacket");

        if(minecraftConnectionClass == null)
            minecraftConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.MinecraftConnection");

        if(connectedPlayerConnectionClass == null)
            connectedPlayerConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.client.ConnectedPlayer");

        if(connectionMethod == null)
            connectionMethod = Reflection.getMethodByName(connectedPlayerConnectionClass, "getConnection");

        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            BRAND = MessageTranslator.replaceMessage(Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0)) + "§r";
            SERVER.getAllPlayers().forEach(this::send);
        } else {
            AtomicInteger animationState = new AtomicInteger(0);
            TASK = SERVER.getScheduler().buildTask(VelocityLoader.getInstance(), () -> {
                if (animationState.getAndIncrement() >= Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().size() - 1)
                    animationState.set(0);
                BRAND = MessageTranslator.replaceMessage(Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(animationState.get())) + "§r";
                SERVER.getAllPlayers().forEach(this::send);
            }).repeat(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY, TimeUnit.MILLISECONDS).schedule();
        }
    }


    @Override
    public void preparePlayer(Object playerObj) { }

    @Override
    public void send(Object playerObj) {
        if (!(playerObj instanceof Player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return;

        try {
            Player player = (Player) playerObj;
            Object connectedPlayerObj = connectedPlayerConnectionClass.cast(player),
                    minecraftConnectionObj = connectionMethod.invoke(connectedPlayerObj);

            String serverName = "", playerName = player.getUsername(), customBrand;
            Optional<ServerConnection> serverConnection = player.getCurrentServer();
            if(serverConnection.isPresent()) serverName = serverConnection.get().getServerInfo().getName();
            customBrand = BRAND.replace("%player%", playerName).replace("%server%", serverName).replace("%ping%", String.valueOf(player.getPing()));

            PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(customBrand, false);
            String brand = player.getProtocolVersion().getProtocol() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand";
            Object pluginMessagePacket = pluginMessagePacketClass.getConstructor(String.class, ByteBuf.class).newInstance(brand, serverBrand.getByteBuf());
            Reflection.getMethodsByParameterAndName(minecraftConnectionObj, "write", Object.class).get(0).invoke(minecraftConnectionObj, pluginMessagePacket);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public PacketUtils.BrandManipulate createPacket(Object playerObj) {
        if (!(playerObj instanceof Player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return null;

        try {
            Player player = (Player) playerObj;

            String serverName = "", playerName = player.getUsername(), customBrand;
            Optional<ServerConnection> serverConnection = player.getCurrentServer();

            if(serverConnection.isPresent()) serverName = serverConnection.get().getServerInfo().getName();
            customBrand = BRAND.replace("%player%", playerName).replace("%server%", serverName).replace("%ping%", String.valueOf(player.getPing()));

            return new PacketUtils.BrandManipulate(customBrand, false);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }
}
