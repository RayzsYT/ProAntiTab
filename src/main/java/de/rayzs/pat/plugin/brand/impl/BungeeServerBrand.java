package de.rayzs.pat.plugin.brand.impl;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.plugin.brand.ServerBrand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
        if(!Storage.USE_CUSTOM_BRAND) return;

        AtomicInteger animationState = new AtomicInteger(0);
        TASK = SERVER.getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
            if(animationState.getAndIncrement() >= Storage.CUSTOM_SERVER_BRANDS.size() - 1) animationState.set(0);
            BRAND = Storage.CUSTOM_SERVER_BRANDS.get(animationState.get());
            SERVER.getPlayers().forEach(this::send);
        }, Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY, Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void preparePlayer(Object playerObj) { }

    @Override
    public void send(Object playerObj) {
        if (!(playerObj instanceof ProxiedPlayer) || !Storage.USE_CUSTOM_BRAND) return;
        ProxiedPlayer player = (ProxiedPlayer) playerObj;

        PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(BRAND.replace("&", "§") + "§r");
        String brand = player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13 ? "minecraft:brand" : "MC|Brand";
        player.sendData(brand, serverBrand.getBytes());
    }
}
