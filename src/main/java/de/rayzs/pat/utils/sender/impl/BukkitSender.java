package de.rayzs.pat.utils.sender.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (MessageTranslator.isSupported()) {
            MessageTranslator.send(sender, message);
            return;
        }

        // To prevent color-code overlapping on Bukkit consoles.
        if (Reflection.isCraftbukkit() && isConsole()) {
            String[] lines = message.split("\n");

            for (String line : lines) {
                sender.sendMessage(MessageTranslator.replaceMessage(sender, line));
            }

            return;
        }

        sender.sendMessage(MessageTranslator.replaceMessage(sender, message));
    }

    @Override
    public Object getSenderObject() {
        return sender;
    }
}
