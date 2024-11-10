package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.response.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class OldBukkitAction implements Action {

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) command = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;

        title = PlaceholderReplacer.replace(player, title.replace("%player%", player.getName()));
        subTitle = PlaceholderReplacer.replace(player, subTitle.replace("%player%", player.getName()));

        player.sendTitle(title, subTitle);
    }

    @Override
    public void addPotionEffect(String action, UUID uuid, String potionEffectTypeName, int duration, int amplifier) {
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
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
        if(player == null) return;

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
}
