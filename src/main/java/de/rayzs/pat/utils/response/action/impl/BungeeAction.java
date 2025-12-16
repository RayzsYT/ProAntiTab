package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.utils.response.action.Action;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (player != null) {
            message = message.replace("%player%", player.getName());
        }

        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), message);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if (player == null) {
            return;
        }

        message = StringUtils.replace(message, "%player%", player.getName());
        message = ResponseHandler.replaceArgsVariables(message, command);

        ProxyServer.getInstance().getPluginManager().dispatchCommand(player, message);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if (player == null) {
            return;
        }

        title = ResponseHandler.replaceArgsVariables(title, command);
        subTitle = ResponseHandler.replaceArgsVariables(subTitle, command);

        title = StringUtils.replace(title, "%player%", player.getName());
        subTitle = StringUtils.replace(subTitle, "%player%", player.getName());

        MessageTranslator.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
    }

    @Override
    public void playSound(String action, UUID uuid, String soundName, float volume, float pitch) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if (player == null) {
            return;
        }

        try {
            MessageTranslator.playSound(player, soundName, volume, pitch);
        } catch (Exception exception) {
            Logger.warning("! Failed to read action: " + action);
            Logger.warning("  > The sound \"" + soundName + "\" could not be found!");
            Logger.warning("  > Here's an example of an existing sound effect:");
            Logger.warning("  > entity.ender_dragon.growl");
        }
    }

    @Override
    public void sendActionbar(String action, UUID uuid, String command, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if (player == null) {
            return;
        }

        message = StringUtils.replace(message, "%player%", player.getName());
        message = ResponseHandler.replaceArgsVariables(message, command);
        MessageTranslator.sendActionbar(player, message);
    }
}
