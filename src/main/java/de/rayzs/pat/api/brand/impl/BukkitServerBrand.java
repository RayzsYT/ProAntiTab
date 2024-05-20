package de.rayzs.pat.api.brand.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.netty.PacketAnalyzer;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.brand.ServerBrand;
import de.rayzs.pat.utils.message.MessageTranslator;
import io.netty.channel.Channel;
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
    private static String BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0);
    private static int TASK = -1;
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

        if(TASK != -1) {
            Bukkit.getScheduler().cancelTask(TASK);
            TASK = -1;
        }

        if(!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED || Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) return;

        AtomicInteger animationState = new AtomicInteger(0);
        TASK = Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitLoader.getPlugin(), () -> {
            if(animationState.getAndIncrement() >= Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().size() - 1) animationState.set(0);
            BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(animationState.get()) + "§r";
            Bukkit.getOnlinePlayers().forEach(this::send);
        }, 1, Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY);
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
            Channel channel = PacketAnalyzer.INJECTED_PLAYERS.get(player.getUniqueId());
            if(channel == null) return;

            Object brandPayloadObj = brandPayloadClass.getDeclaredConstructor(String.class).newInstance(MessageTranslator.replaceMessage(player, customBrand)),
                    customPacketPayloadPacket = clientBoundCustomPacketPayloadPacketClass.getDeclaredConstructor(customPacketPayloadPacketClass).newInstance(brandPayloadObj);
            channel.pipeline().writeAndFlush(customPacketPayloadPacket);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
