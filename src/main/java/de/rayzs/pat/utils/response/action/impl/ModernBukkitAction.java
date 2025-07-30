package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.utils.response.action.Action;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class ModernBukkitAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command, String message) {
        Player player = Bukkit.getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (player != null)
            message = message.replace("%player%", player.getName());

        if (message.contains("%"))
            message = PlaceholderReplacer.replace(player, message);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command, String message) {
        Player player = Bukkit.getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if(player != null)
            message = message.replace("%player%", player.getName());

        if (message.contains("%"))
            message = PlaceholderReplacer.replace(player, message);

        if (player != null)
            Bukkit.dispatchCommand(player, message);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String command, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        Player player = Bukkit.getPlayer(uuid);
        title = ResponseHandler.replaceArgsVariables(title, command);
        subTitle = ResponseHandler.replaceArgsVariables(subTitle, command);

        if (player == null)
            return;

        title = PlaceholderReplacer.replace(player, StringUtils.replace(title, "&", "§", "%player%", player.getName()));
        subTitle = PlaceholderReplacer.replace(player, StringUtils.replace(subTitle, "&", "§", "%player%", player.getName()));

        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

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
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception exception) {
            Logger.warning("! Failed to read action: " + action);
            Logger.warning("  > The sound \"" + soundName + "\" could not be found!");
            Logger.warning("  > Here's an example of an existing sound effect:");
            Logger.warning("  > ENTITY_ENDER_DRAGON_GROWL");
        }
    }

    @Override
    public void sendActionbar(String action, UUID uuid, String command, String message) {
        Player player = Bukkit.getPlayer(uuid);
        message = ResponseHandler.replaceArgsVariables(message, command);

        if (player == null)
            return;

        message = PlaceholderReplacer.replace(player, StringUtils.replace(message, "&", "§", "%player%", player.getName()));

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
