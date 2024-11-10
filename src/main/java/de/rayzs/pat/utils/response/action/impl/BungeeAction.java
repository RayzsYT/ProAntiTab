package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.response.action.Action;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if(player != null) command = command.replace("%player%", player.getName());

        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if(player == null) return;

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
    public void sendActionbar(String action, UUID uuid, String text) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if(player == null) return;

        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(StringUtils.replace(text, "%player%", player.getName())));
    }
}
