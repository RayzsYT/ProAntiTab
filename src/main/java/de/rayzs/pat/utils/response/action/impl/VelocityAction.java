package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.utils.response.action.Action;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.StringUtils;

import java.util.*;

public class VelocityAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (optPlayer.isPresent()) {
            message = message.replace("%player%", optPlayer.get().getUsername());
        }

        VelocityLoader.getServer().getCommandManager().executeAsync(VelocityLoader.getServer().getConsoleCommandSource(), message);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command, String message) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);

        if (optPlayer.isEmpty()) {
            return;
        }

        Player player = optPlayer.get();

        message = message.replace("%player%", player.getUsername());
        message = ResponseHandler.replaceArgsVariables(message, command);

        VelocityLoader.getServer().getCommandManager().executeAsync(player, message);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);

        if (optPlayer.isEmpty()) {
            return;
        }

        Player player = optPlayer.get();

        title = ResponseHandler.replaceArgsVariables(title, command);
        subTitle = ResponseHandler.replaceArgsVariables(subTitle, command);

        title = StringUtils.replace(title,  "%player%", player.getUsername());
        subTitle = StringUtils.replace(subTitle, "%player%", player.getUsername());

        MessageTranslator.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
    }

    @Override
    public void playSound(String action, UUID uuid, String soundName, float volume, float pitch) {
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);

        if (optPlayer.isEmpty()) {
            return;
        }

        Player player = optPlayer.get();

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
        Optional<Player> optPlayer = VelocityLoader.getServer().getPlayer(uuid);

        if (optPlayer.isEmpty()) {
            return;
        }

        Player player = optPlayer.get();

        message = ResponseHandler.replaceArgsVariables(message, command);
        message = StringUtils.replace(message, "%player%", player.getUsername());

        MessageTranslator.sendActionbar(player, message);
    }
}
