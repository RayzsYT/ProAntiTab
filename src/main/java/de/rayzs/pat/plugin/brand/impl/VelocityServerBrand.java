package de.rayzs.pat.plugin.brand.impl;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.plugin.brand.ServerBrand;
import de.rayzs.pat.utils.message.MessageTranslator;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VelocityServerBrand implements ServerBrand {

    private static final ProxyServer SERVER = VelocityLoader.getServer();
    private static Class<?> pluginMessagePacketClass, minecraftConnectionClass, connectedPlayerConnectionClass;
    private static ScheduledTask TASK;
    private static String BRAND = Storage.CUSTOM_SERVER_BRANDS.get(0);

    @Override
    public void initializeTask() {
        if(TASK != null) TASK.cancel();
        if(!Storage.USE_CUSTOM_BRAND) return;

        if(pluginMessagePacketClass == null)
            pluginMessagePacketClass = Reflection.getClass("com.velocitypowered.proxy.protocol.packet.PluginMessagePacket");

        if(minecraftConnectionClass == null)
            minecraftConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.MinecraftConnection");

        if(connectedPlayerConnectionClass == null)
            connectedPlayerConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.client.ConnectedPlayer");

        AtomicInteger animationState = new AtomicInteger(0);
        TASK = SERVER.getScheduler().buildTask(VelocityLoader.getInstance(), () -> {
            if(animationState.getAndIncrement() >= Storage.CUSTOM_SERVER_BRANDS.size() - 1) animationState.set(0);
            BRAND = MessageTranslator.replaceMessage(Storage.CUSTOM_SERVER_BRANDS.get(animationState.get())) + "§r";
            SERVER.getAllPlayers().forEach(this::send);
        }).repeat(Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY, TimeUnit.MILLISECONDS).schedule();
    }


    @Override
    public void preparePlayer(Object playerObj) { }

    @Override
    public void send(Object playerObj) {
        if (!(playerObj instanceof Player) || !Storage.USE_CUSTOM_BRAND) return;

        try {
            Player player = (Player) playerObj;
            Object connectedPlayerObj = connectedPlayerConnectionClass.cast(player),
                    minecraftConnectionObj = Reflection.getMethodsByName(connectedPlayerObj, "getConnection").get(0).invoke(connectedPlayerObj);

            String serverName = "", playerName = player.getUsername();
            Optional<ServerConnection> serverConnection = player.getCurrentServer();
            if(serverConnection.isPresent()) serverName = serverConnection.get().getServerInfo().getName();

            PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(BRAND.replace("%player%", playerName).replace("%server%", serverName), false);
            String brand = player.getProtocolVersion().getProtocol() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand";
            Object pluginMessagePacket = pluginMessagePacketClass.getConstructor(String.class, ByteBuf.class).newInstance(brand, serverBrand.getByteBuf());
            Reflection.getMethodsByParameterAndName(minecraftConnectionObj, "write", Object.class).get(0).invoke(minecraftConnectionObj, pluginMessagePacket);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
