package de.rayzs.pat.utils;

import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.connection.*;
import org.bukkit.entity.Player;
import java.util.UUID;

public class CommandSender {

    private final Object sender;
    private static final UUID CONSOLE_UUID = UUID.randomUUID();

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

    public boolean isOperator() {
        if(isConsole()) return true;

        if(!Reflection.isProxyServer()) {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                return player.isOp();
            }
        }

        return false;
    }

    public Object getSenderObject() {
        return sender;
    }

    public void sendMessage(String text) {
        if(MessageTranslator.isSupported()) MessageTranslator.send(sender, text);
        else if(Reflection.isProxyServer()) {
          if(!Reflection.isVelocityServer()) {
              if (sender instanceof net.md_5.bungee.api.CommandSender)
                  ((net.md_5.bungee.api.CommandSender) sender).sendMessage(MessageTranslator.replaceMessage(sender, text));
          }
        } else if(sender instanceof Player) ((Player) sender).sendMessage(MessageTranslator.replaceMessage(sender, text));
        else if(sender instanceof org.bukkit.command.CommandSender) ((org.bukkit.command.CommandSender) sender).sendMessage(MessageTranslator.replaceMessage(sender, text));
    }

    public String getServerName() {
        if(Reflection.isVelocityServer()) return sender instanceof com.velocitypowered.api.proxy.Player ? ((com.velocitypowered.api.proxy.Player) sender).getCurrentServer().get().getServer().getServerInfo().getName() : Storage.SERVER_NAME;
        else if(Reflection.isProxyServer()) return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getServer().getInfo().getName() : Storage.SERVER_NAME;
        return Storage.SERVER_NAME;
    }

    public String getName() {
        if(Reflection.isVelocityServer()) return sender instanceof com.velocitypowered.api.proxy.Player ? ((com.velocitypowered.api.proxy.Player) sender).getUsername() : "CONSOLE";
        else if(Reflection.isProxyServer()) return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getName() : "CONSOLE";
        return sender instanceof Player ? ((Player) sender).getName() : "CONSOLE";
    }

    public UUID getUniqueId() {
        if(Reflection.isVelocityServer()) return sender instanceof com.velocitypowered.api.proxy.Player ? ((com.velocitypowered.api.proxy.Player) sender).getUniqueId() : CONSOLE_UUID;
        else if(Reflection.isProxyServer()) return  sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : CONSOLE_UUID;
        else if(sender instanceof Player) return ((Player) sender).getUniqueId();
        return CONSOLE_UUID;
    }
}