package de.rayzs.pat.utils.sender.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeSender extends CommandSenderAbstract {

    private final CommandSender sender;
    private final UUID uuid;
    private final String name;

    private final boolean console;

    public BungeeSender(Object senderObj) {
        super(senderObj);

        if (senderObj instanceof ProxiedPlayer player) {
            this.sender = player;

            this.name = player.getName();
            this.uuid = player.getUniqueId();
            this.console = false;

            updateGroups();
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
        if (MessageTranslator.isSupported()) {
            MessageTranslator.send(sender, message);
            return;
        }

        sender.sendMessage(MessageTranslator.replaceMessage(sender, message));
    }

    @Override
    public void updateGroups() {
        setGroups(GroupManager.getGroups());
    }
}
