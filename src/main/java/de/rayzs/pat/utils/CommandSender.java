package de.rayzs.pat.utils;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        return Reflection.isVelocityServer() ? sender instanceof com.velocitypowered.api.proxy.Player
                : Reflection.isProxyServer() ? sender instanceof ProxiedPlayer
                : sender instanceof Player;
    }

    public boolean hasPermission(String permission) {
        if(isConsole()) return true;
        return Reflection.isVelocityServer()
                && sender instanceof CommandSource
                ? ((CommandSource) sender).hasPermission(permission)
                : Reflection.isProxyServer()
                && sender instanceof ProxiedPlayer
                ? ((ProxiedPlayer) sender).hasPermission(permission)
                : sender instanceof Player
                && (((Player) sender).hasPermission(permission)
                || ((Player) sender).isOp());
    }

    public void sendMessage(String text) {
        if(Reflection.isVelocityServer()) ((CommandSource) sender).sendMessage(MiniMessage.miniMessage().deserialize(MessageTranslator.translate(text)));
        else if(Reflection.isProxyServer()) ((net.md_5.bungee.api.CommandSender) sender).sendMessage(text);
        else ((org.bukkit.command.CommandSender) sender).sendMessage(text);
    }

    public UUID getUniqueId() {
        if(Reflection.isVelocityServer()) return ((com.velocitypowered.api.proxy.Player) sender).getUniqueId();
        else if(Reflection.isProxyServer()) return ((ProxiedPlayer) sender).getUniqueId();
        else return ((Player) sender).getUniqueId();
    }
}