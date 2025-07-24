package de.rayzs.pat.utils.response.action.impl;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.response.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

public class OldBukkitAction implements Action {

    private String versionPackage = null;

    @Override
    public void executeConsoleCommand(String action, UUID uuid, String command) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) command = command.replace("%player%", player.getName());

        if (command.contains("%"))
            command = PlaceholderReplacer.replace(player, command);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void executePlayerCommand(String action, UUID uuid, String command) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null)
            command = command.replace("%player%", player.getName());

        if (command.contains("%"))
            command = PlaceholderReplacer.replace(player, command);

        if (player != null)
            Bukkit.dispatchCommand(player, command);
    }

    @Override
    public void sendTitle(String action, UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        title = PlaceholderReplacer.replace(player, StringUtils.replace(title, "&", "§", "%player%", player.getName()));
        subTitle = PlaceholderReplacer.replace(player, StringUtils.replace(subTitle, "&", "§", "%player%", player.getName()));

        player.sendTitle(title, subTitle);
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

        if (potionEffectType == null) {
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

        if(player == null)
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
    public void sendActionbar(String action, UUID uuid, String text) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        text = PlaceholderReplacer.replace(player, StringUtils.replace(text, "&", "§", "%player%", player.getName()));

        try {
            if (versionPackage == null)
                versionPackage = Reflection.getVersionPackageName();

            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Constructor<?> constructor = (Objects.<Class<?>>requireNonNull(Class.forName("net.minecraft.server." + versionPackage + ".PacketPlayOutChat"))).getConstructor(Class.forName("net.minecraft.server." + versionPackage + ".IChatBaseComponent"), byte.class);
            Object iChatBaseComponent = (Objects.requireNonNull(Class.forName("net.minecraft.server." + versionPackage + ".IChatBaseComponent"))).getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + text + "\"}");
            Object actionbarPacket = constructor.newInstance(iChatBaseComponent, (byte) 2);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + versionPackage + ".Packet")).invoke(playerConnection, actionbarPacket);

        } catch (ClassNotFoundException
                | NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchFieldException
                | InstantiationException exception) {
            Logger.warning("! Failed to run action: " + action);
            Logger.warning("  > Actionbars are not supported in this server version!");
        }
    }
}
