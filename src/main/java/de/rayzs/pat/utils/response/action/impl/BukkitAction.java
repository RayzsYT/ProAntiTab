package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.MessageHelper;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.utils.response.action.Action;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class BukkitAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        final CommandSender sender = CommandSender.from(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (sender != null) {
            message = StringUtils.replace(message, "%player%", sender.getName());
        }

        if (message.contains("%")) {
            message = PlaceholderReplacer.applyPlaceholders(sender, message);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command, String message) {
        final CommandSender sender = CommandSender.from(uuid);

        if (sender == null) {
            return;
        }

        message = StringUtils.replace(message, "%player%", sender.getName());
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (message.contains("%")) {
            message = PlaceholderReplacer.applyPlaceholders(sender, message);
        }

        sender.execute(message);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        final CommandSender sender = CommandSender.from(uuid);

        if (sender == null) {
            return;
        }

        title = ResponseHandler.replaceArgsVariables(title, command);
        subTitle = ResponseHandler.replaceArgsVariables(subTitle, command);

        title = StringUtils.replace(title, "%player%", sender.getName());
        subTitle = StringUtils.replace(subTitle, "%player%", sender.getName());

        MessageHelper.sendTitle(sender, title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        PotionEffectType potionEffectType = null;
        try {
            potionEffectType = PotionEffectType.getByName(potionEffectTypeName);
        } catch (Exception ignored) { }

        if(potionEffectType == null) {
            Logger.warning("! Failed to read action: " + action);
            Logger.warning("  > The effect \"" + potionEffectTypeName + "\" could not be found!");
            Logger.warning("  > Here's an example of an existing effect:");
            Logger.warning("  > BLINDNESS, REGENERATION");
            return;
        }

        player.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
    }

    @Override
    public void playSound(String action, UUID uuid, String soundName, float volume, float pitch) {
        final CommandSender sender = CommandSender.from(uuid);

        if (sender == null) {
            return;
        }

        try {
            MessageHelper.playSound(sender, soundName, volume, pitch);
        } catch (Exception exception) {
            Logger.warning("! Failed to read action: " + action);
            Logger.warning("  > The sound \"" + soundName + "\" could not be found!");
            Logger.warning("  > Here's an example of an existing sound effect:");
            Logger.warning("  > ENTITY_ENDER_DRAGON_GROWL");
        }
    }

    @Override
    public void sendActionbar(String action, UUID uuid, String command, String message) {
        final CommandSender sender = CommandSender.from(uuid);

        if (sender == null) {
            return;
        }

        message = StringUtils.replace(message, "%player%", sender.getName());
        message = ResponseHandler.replaceArgsVariables(message, command);

        MessageHelper.sendActionbar(sender, message);
    }
}
