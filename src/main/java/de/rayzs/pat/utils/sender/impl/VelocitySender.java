package de.rayzs.pat.utils.sender.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;

import java.util.Optional;
import java.util.UUID;

public class VelocitySender extends CommandSenderAbstract {


    public static VelocitySender from(final Object obj) {
        return obj instanceof UUID uuid ? from(uuid)
                : obj instanceof Player player ? from(player)
                : obj instanceof CommandSource sender ? from(sender)
                : null;
    }

    public static VelocitySender from(final UUID uuid) {
        final Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);
        if (optPlayer.isEmpty()) return null;

        final Player player = optPlayer.get();
        return new VelocitySender(player, player.getUniqueId(), player.getUsername(), false);
    }


    public static VelocitySender from(final Player player) {
        return player != null ?
                new VelocitySender(player, player.getUniqueId(), player.getUsername(), false)
                : null;
    }


    public static VelocitySender from(final CommandSource sender) {
        if (sender instanceof Player player) {
            return from(player);
        }

        return sender != null ?
                new VelocitySender(sender, CONSOLE_UUID, "CONSOLE", true)
                : null;
    }



    private final UUID uuid;
    private final String name;
    private final CommandSource sender;
    private final boolean console;


    private VelocitySender(
            final CommandSource sender,
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
        return this.console;
    }

    @Override
    public boolean isPlayer() {
        return !this.console;
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
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getServerName() {
        return console
                ? Storage.SERVER_NAME
                : Storage.getLoader().getPlayerServerName(this.uuid);
    }

    @Override
    public void sendMessage(String message) {
        MessageTranslator.send(sender, message);
    }

    @Override
    public Object getSenderObject() {
        return sender;
    }
}
