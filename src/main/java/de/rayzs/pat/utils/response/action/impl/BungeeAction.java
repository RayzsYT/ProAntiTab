package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.utils.response.action.Action;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (player != null)
            message = message.replace("%player%", player.getName());

        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), message);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (player != null)
            message = command.replace("%player%", player.getName());

        ProxyServer.getInstance().getPluginManager().dispatchCommand(player, message);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        title = ResponseHandler.replaceArgsVariables(title, command);
        subTitle = ResponseHandler.replaceArgsVariables(subTitle, command);

        if (player == null)
            return;

        Title titleObj = ProxyServer.getInstance().createTitle();
        titleObj.title(TextComponent.fromLegacyText(StringUtils.replace(title, "&", "§", "%player%", player.getName())));
        titleObj.subTitle(TextComponent.fromLegacyText(StringUtils.replace(subTitle, "&", "§", "%player%", player.getName())));
        titleObj.fadeIn(fadeIn);
        titleObj.stay(stay);
        titleObj.fadeOut(fadeOut);
        player.sendTitle(titleObj);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
    }

    @Override
    public void playSound(String action, UUID uuid, String soundName, float volume, float pitch) {
    }

    @Override
    public void sendActionbar(String action, UUID uuid, String command, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (player == null)
            return;

        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(StringUtils.replace(message, "%player%", player.getName())));
    }
}
