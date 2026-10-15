package de.rayzs.pat.utils.sender.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.MessageHelper;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

public class BukkitSender extends CommandSenderAbstract {

    public static BukkitSender from(final Object obj) {
        return obj instanceof UUID uuid ? from(uuid)
                : obj instanceof Player player ? from(player)
                : obj instanceof CommandSender sender ? from(sender)
                : null;
    }

    public static BukkitSender from(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        return player != null
                ? new BukkitSender(player, uuid, player.getName(), false)
                : null;
    }


    public static BukkitSender from(final Player player) {
        return player != null ?
                new BukkitSender(player, player.getUniqueId(), player.getName(), false)
                : null;
    }


    public static BukkitSender from(final CommandSender sender) {
        if (sender instanceof Player player) {
            return from(player);
        }

        return sender != null ?
                new BukkitSender(sender, CONSOLE_UUID, sender.getName(), true)
                : null;
    }


    private final UUID uuid;
    private final String name;
    private final boolean console;
    private final CommandSender sender;

    private BukkitSender(
            final CommandSender sender,
            final UUID uuid,
            final String name,
            final boolean console
    ) {
        this.uuid = uuid;
        this.name = name;
        this.console = console;
        this.sender = sender;
    }

    @Override
    public boolean isConsole() {
        return console;
    }

    @Override
    public boolean isPlayer() {
        return !console;
    }

    @Override
    public boolean isOperator() {
        return sender.isOp();
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getServerName() {
        return Storage.SERVER_NAME;
    }

    @Override
    public void sendMessage(String message) {
        if (MessageHelper.isSupported()) {
            MessageHelper.sendMessage(this, message);
            return;
        }

        // To prevent color-code overlapping on Bukkit consoles.
        if (Reflection.isBukkit() && isConsole()) {
            String[] lines = message.split("\n");

            for (String line : lines) {
                sender.sendMessage(PlaceholderReplacer.applyPlaceholders(this, line));
            }

            return;
        }

        sender.sendMessage(PlaceholderReplacer.applyPlaceholders(this, message));
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (isPlayer()) return;

        if (MessageHelper.isSupported()) {
            MessageHelper.sendTitle(this, title, subtitle, 10, 10, 10);
            return;
        }

        if (Reflection.isAfter(1, 11, 0)) {
            ((Player) sender).sendTitle(
                    PlaceholderReplacer.applyPlaceholders(this, title),
                    PlaceholderReplacer.applyPlaceholders(this, subtitle),
                    fadeIn, stay, fadeOut
            );
        } else {
            ((Player) sender).sendTitle(
                    PlaceholderReplacer.applyPlaceholders(this, title),
                    PlaceholderReplacer.applyPlaceholders(this, subtitle)
            );
        }
    }

    @Override
    public void execute(String command) {
        Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public void sendActionbar(String message) {
        if (MessageHelper.isSupported()) {
            MessageHelper.sendActionbar(this, message);
            return;
        }


        if (Reflection.isAtMost(1, 16)) {
            try {
                final Object entityPlayer = sender.getClass().getMethod("getHandle").invoke(sender);
                final Constructor<?> constructor = (Objects.<Class<?>> requireNonNull(Class.forName("net.minecraft.server." + Reflection.getVersionPackageName() + ".PacketPlayOutChat")))
                        .getConstructor(
                                Class.forName("net.minecraft.server." + Reflection.getVersionPackageName() + ".IChatBaseComponent"),
                                byte.class
                        );

                final Object iChatBaseComponent = (Objects.requireNonNull(Class.forName("net.minecraft.server." + Reflection.getVersionPackageName() + ".IChatBaseComponent")))
                        .getDeclaredClasses()[0]
                        .getMethod("a", String.class)
                        .invoke(null, "{\"text\":\"" + message + "\"}");

                final Object actionbarPacket = constructor.newInstance(iChatBaseComponent, (byte) 2);
                final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

                playerConnection.getClass().getMethod(
                        "sendPacket",
                        Class.forName("net.minecraft.server." + Reflection.getVersionPackageName() + ".Packet")
                ).invoke(playerConnection, actionbarPacket);

                return;


            } catch (ClassNotFoundException
                     | NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException
                     | NoSuchFieldException
                     | InstantiationException ignored) { }
        }

        Logger.warning("Actionbars are not supported in this server version!");
    }

    @Override
    public Object getSenderObject() {
        return sender;
    }
}
