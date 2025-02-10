package de.rayzs.pat.api.brand.impl;

import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.utils.message.MessageTranslator;
import java.util.concurrent.atomic.AtomicInteger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.scheduler.PATSchedulerTask;
import io.netty.channel.Channel;
import de.rayzs.pat.api.brand.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import de.rayzs.pat.utils.*;
import java.lang.reflect.*;
import org.bukkit.*;
import java.util.*;

public class BukkitServerBrand implements ServerBrand {

    private static final List<Player> MODIFIED_BRAND_PLAYERS = new ArrayList<>();
    private static final Server SERVER = Bukkit.getServer();
    private static String BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0);
    private static PATSchedulerTask  TASK;
    private static boolean INITIALIZED = false;
    private static Class<?> brandPayloadClass, clientBoundCustomPacketPayloadPacketClass, customPacketPayloadPacketClass;

    @Override
    public void initializeTask() {

        if(Reflection.isWeird()) {
            if (brandPayloadClass == null)
                brandPayloadClass = Reflection.getClass("net.minecraft.network.protocol.common.custom.BrandPayload");

            if (clientBoundCustomPacketPayloadPacketClass == null)
                clientBoundCustomPacketPayloadPacketClass = Reflection.getClass("net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket");

            if (customPacketPayloadPacketClass == null)
                customPacketPayloadPacketClass = Reflection.getClass("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
        }

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

        if(TASK != null && TASK.isActive()) {
            TASK.cancelTask();
            TASK = null;
            MODIFIED_BRAND_PLAYERS.clear();
        }

        if(!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return;

        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0) + "§r";

            PATScheduler.createAsyncScheduler(() -> {
                Bukkit.getOnlinePlayers().forEach(this::send);
            }, 1, 1);

        } else {
            AtomicInteger animationState = new AtomicInteger(0);
            TASK = PATScheduler.createAsyncScheduler(() -> {
                if (animationState.getAndIncrement() >= Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().size() - 1)
                    animationState.set(0);
                BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(animationState.get()) + "§r";
                Bukkit.getOnlinePlayers().forEach(this::send);
            }, 1, Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY);
        }
    }

    @Override
    public void preparePlayer(Object playerObj) {
        if(!(playerObj instanceof Player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return;
        Player player = (Player) playerObj;

        try {
            Field channelsField = Reflection.getFieldByName(player.getClass(), "channels");
            Set<String> channels = (Set<String>) channelsField.get(player);
            channels.add(CustomServerBrand.CHANNEL_NAME);
            Reflection.closeAccess(channelsField);
            if(!MODIFIED_BRAND_PLAYERS.contains(player)) MODIFIED_BRAND_PLAYERS.add(player);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void send(Object playerObj) {
        if(!(playerObj instanceof Player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) return;
        Player player = (Player) playerObj;
        String playerName = player.getName(), displayName = player.getDisplayName(), worldName = player.getWorld().getName(),
                customBrand = BRAND.replace("%player%", playerName).replace("%displayname%", displayName).replace("%world%", worldName);

        if(!Reflection.isWeird()) {
            PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(MessageTranslator.replaceMessage(player, customBrand));
            player.sendPluginMessage(BukkitLoader.getPlugin(), CustomServerBrand.CHANNEL_NAME, serverBrand.getBytes());
            return;
        }

        try {
            Channel channel = BukkitPacketAnalyzer.INJECTED_PLAYERS.get(player.getUniqueId());
            if(channel == null) return;

            Object brandPayloadObj = brandPayloadClass.getDeclaredConstructor(String.class).newInstance(MessageTranslator.replaceMessage(player, customBrand)),
                    customPacketPayloadPacket = clientBoundCustomPacketPayloadPacketClass.getDeclaredConstructor(customPacketPayloadPacketClass).newInstance(brandPayloadObj);
            channel.pipeline().writeAndFlush(customPacketPayloadPacket);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public PacketUtils.BrandManipulate createPacket(Object playerObj) {
        return null;
    }

    private boolean isModified(Player player) {
        if(!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED || Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return false;
        return MODIFIED_BRAND_PLAYERS.contains(player);
    }

    public static void removeFromModified(Player player) {
        MODIFIED_BRAND_PLAYERS.remove(player);
    }
}
