package de.rayzs.pat.utils.sender.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitSender extends CommandSenderAbstract {

    private final UUID uuid;
    private final String name;
    private final boolean console;

    private CommandSender sender;

    public BukkitSender(Object senderObj) {
        super(senderObj);

        if (senderObj instanceof UUID uuid) {
            final Player player = Bukkit.getPlayer(uuid);

            this.uuid = uuid;
            this.console = false;

            this.sender = player;
            this.name = player.getName();
            return;
        }

        if (senderObj instanceof Player player) {
            this.sender = player;

            this.name = player.getName();
            this.uuid = player.getUniqueId();
            this.console = false;

            return;
        }

        this.sender = (CommandSender) senderObj;

        if (sender != null) {
            this.name = sender.getName();
            this.uuid = CommandSenderHandler.CONSOLE_UUID;
        } else {
            this.name = null;
            this.uuid = null;
        }

        this.console = true;
    }

    @Override
    public void updateSenderObject(Object senderObj) {
        super.updateSenderObject(senderObj);

        if (senderObj instanceof Player player) {
            this.sender = player;
        } else if (senderObj instanceof CommandSender commandSender) {
            this.sender = commandSender;
        }
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
        return sender.isOp();
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
}
