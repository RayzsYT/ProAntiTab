package de.rayzs.pat.utils.brand.impl;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.utils.brand.CustomServerBrand;
import de.rayzs.pat.utils.brand.ServerBrand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BukkitServerBrand implements ServerBrand {

    private static final Server SERVER = Bukkit.getServer();
    private static String BRAND = "";
    private static int TASK = -1;
    private static boolean INITIALIZED = false;

    @Override
    public void initializeTask() {
        Bukkit.getOnlinePlayers().forEach(this::preparePlayer);
        if(!INITIALIZED)
            try {
                Method method = Reflection.getMethodsByParameterAndName(SERVER.getMessenger(), "addToOutgoing", Plugin.class, String.class).get(0);
                Reflection.invokeMethode(method, SERVER.getMessenger(), BukkitLoader.getPlugin(), CustomServerBrand.CHANNEL_NAME);
                SERVER.getMessenger().registerOutgoingPluginChannel(BukkitLoader.getPlugin(), CustomServerBrand.CHANNEL_NAME);
                INITIALIZED = true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        if(TASK != -1) {
            Bukkit.getScheduler().cancelTask(TASK);
            TASK = -1;
        }
        if(!Storage.USE_CUSTOM_BRAND) return;

        AtomicInteger animationState = new AtomicInteger(0);
        TASK = Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitLoader.getPlugin(), () -> {
            if(animationState.getAndIncrement() >= Storage.CUSTOM_SERVER_BRANDS.size() - 1) animationState.set(0);
            BRAND = Storage.CUSTOM_SERVER_BRANDS.get(animationState.get());
            Bukkit.getOnlinePlayers().forEach(this::send);
        }, Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY, Storage.CUSTOM_SERVER_BRAND_REPEAT_DELAY);

    }

    @Override
    public void preparePlayer(Object playerObj) {
        if(!(playerObj instanceof Player) || !Storage.USE_CUSTOM_BRAND) return;
        Player player = (Player) playerObj;

        try {
            Field channelsField = Reflection.getFieldByName(player.getClass(), "channels");
            Set<String> channels = (Set<String>) channelsField.get(player);
            channels.add(CustomServerBrand.CHANNEL_NAME);
            Reflection.closeAccess(channelsField);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void send(Object playerObj) {
        if(!(playerObj instanceof Player) || !Storage.USE_CUSTOM_BRAND) return;
        Player player = (Player) playerObj;

        PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(BRAND.replace("&", "§") + "§r");
        player.sendPluginMessage(BukkitLoader.getPlugin(), CustomServerBrand.CHANNEL_NAME, serverBrand.getBytes());

    }
}
