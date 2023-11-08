package de.rayzs.pat.utils;

import net.md_5.bungee.api.connection.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandSender {

    private final Object sender;

    public CommandSender(Object sender) {
        this.sender = sender;
    }

    public boolean isConsole() {
        return !isPlayer();
    }

    public boolean isPlayer() {
        return Reflection.isBungeecordServer() ? sender instanceof ProxiedPlayer : sender instanceof Player;
    }

    public boolean hasPermission(String permission) {
        if(isConsole()) return true;
        return Reflection.isBungeecordServer()
                && sender instanceof ProxiedPlayer
                ? ((ProxiedPlayer) sender).hasPermission(permission)
                : sender instanceof Player
                && (((Player) sender).hasPermission(permission)
                || ((Player) sender).isOp());
    }

    public void sendMessage(String text) {
        if(Reflection.isBungeecordServer()) ((net.md_5.bungee.api.CommandSender) sender).sendMessage(text);
        else ((org.bukkit.command.CommandSender) sender).sendMessage(text);
    }

    public UUID getUniqueId() {
        if(Reflection.isBungeecordServer()) return ((ProxiedPlayer) sender).getUniqueId();
        else return ((Player) sender).getUniqueId();
    }
}