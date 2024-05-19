package de.rayzs.pat.api.brand.impl;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.api.brand.ServerBrand;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BungeeServerBrand implements ServerBrand {

    private static final ProxyServer SERVER = BungeeLoader.getPlugin().getProxy();
    private static ScheduledTask TASK;
    private static String BRAND;

    public BungeeServerBrand() {
        if (!Storage.USE_CUSTOM_BRAND) return;
    }

    @Override
    public void initializeTask() {
        if(TASK != null) TASK.cancel();
        if(!Storage.USE_CUSTOM_BRAND || Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY == -1) return;

        AtomicInteger animationState = new AtomicInteger(0);
        TASK = SERVER.getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
            if(animationState.getAndIncrement() >= Storage.CUSTOM_SERVER_BRANDS.size() - 1) animationState.set(0);
            BRAND = MessageTranslator.replaceMessage(Storage.CUSTOM_SERVER_BRANDS.get(animationState.get())) + "§r";
            SERVER.getPlayers().forEach(this::send);
        }, 1, Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void preparePlayer(Object playerObj) { }

    @Override
    public void send(Object playerObj) {
        if (!(playerObj instanceof ProxiedPlayer) || !Storage.USE_CUSTOM_BRAND) return;
        ProxiedPlayer player = (ProxiedPlayer) playerObj;

        String serverName = "", playerName = player.getName(), customBrand;
        Server server = player.getServer();
        if(server != null) serverName = server.getInfo().getName();
        customBrand = BRAND.replace("%player%", playerName).replace("%server%", serverName);

        PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(customBrand);
        String brand = player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand";
        player.sendData(brand, serverBrand.getBytes());
    }
}
