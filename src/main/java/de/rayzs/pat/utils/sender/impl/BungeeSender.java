package de.rayzs.pat.utils.sender.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeSender extends CommandSenderAbstract {


    public static BungeeSender from(final Object obj) {
        return obj instanceof UUID uuid ? from(uuid)
                : obj instanceof ProxiedPlayer player ? from(player)
                : obj instanceof CommandSender sender ? from(sender)
                : null;
    }

    public static BungeeSender from(final UUID uuid) {
        final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        return player != null
                ? new BungeeSender(player, uuid, player.getName(), false)
                : null;
    }


    public static BungeeSender from(final ProxiedPlayer player) {
        return player != null ?
                new BungeeSender(player, player.getUniqueId(), player.getName(), false)
                : null;
    }


    public static BungeeSender from(final CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return from(player);
        }

        return sender != null ?
                new BungeeSender(sender, CONSOLE_UUID, sender.getName(), true)
                : null;
    }


    private final UUID uuid;
    private final String name;
    private final boolean console;
    private final CommandSender sender;

    private BungeeSender(
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
        return false;
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
        return console
                ? Storage.SERVER_NAME
                : Storage.getLoader().getPlayerServerName(uuid);
    }

    @Override
    public void sendMessage(String message) {
        if (MessageTranslator.isSupported()) {
            MessageTranslator.send(sender, message);
            return;
        }

        sender.sendMessage(MessageTranslator.replaceMessage(sender, message));
    }

    @Override
    public Object getSenderObject() {
        return sender;
    }
}
