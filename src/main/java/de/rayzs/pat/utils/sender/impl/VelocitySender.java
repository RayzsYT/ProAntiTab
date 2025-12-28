package de.rayzs.pat.utils.sender.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSenderAbstract;
import de.rayzs.pat.utils.sender.CommandSenderHandler;

import java.util.UUID;

public class VelocitySender extends CommandSenderAbstract {

    private final boolean console;

    private final UUID uuid;
    private final String name;

    private CommandSource sender;

    public VelocitySender(Object senderObj) {
        super(senderObj);

        if (senderObj instanceof UUID uuid) {
            final Player player = VelocityLoader.getServer().getPlayer(uuid).get();

            this.uuid = uuid;
            this.console = false;

            this.sender = player;
            this.name = player.getUsername();
            return;
        }

        if (senderObj instanceof Player player) {
            this.sender = player;

            this.name = player.getUsername();
            this.uuid = player.getUniqueId();
            this.console = false;
            return;
        }

        this.sender = (CommandSource) senderObj;

        if (sender != null) {
            this.name = "";
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
            sender = player;
        } else if (senderObj instanceof CommandSource commandSource) {
            sender = commandSource;
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
}
