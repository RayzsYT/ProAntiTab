package de.rayzs.pat.utils.message.translators;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.audience.Audience;
import de.rayzs.pat.plugin.BukkitLoader;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import de.rayzs.pat.utils.message.*;
import org.bukkit.entity.Player;

import java.time.Duration;

public class BukkitMessageTranslator implements Translator {

    private BukkitAudiences audiences;

    public BukkitMessageTranslator() {
        audiences = BukkitAudiences.create(BukkitLoader.getPlugin());
    }

    @Override
    public String strip(String text) {
        return ChatColor.stripColor(miniMessage.stripTags(text));
    }

    @Override
    public void send(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
        final Audience audience = sender.getSenderObject() instanceof Player player
                ? audiences.player(player)
                : sender.getSenderObject() instanceof CommandSender commandSender
                ? audiences.sender(commandSender)
                : null;


        if (audience == null) {
            return;
        }

        audience.sendMessage(toComponent(text));
    }

    @Override
    public void sendActionbar(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
        final Audience audience = sender.getSenderObject() instanceof Player player
                ? audiences.player(player)
                : sender.getSenderObject() instanceof CommandSender commandSender
                ? audiences.sender(commandSender)
                : null;


        if (audience == null) {
            return;
        }


        audience.sendActionBar(toComponent(text));
    }

    @Override
    public void sendTitle(de.rayzs.pat.utils.sender.CommandSender sender, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {

        final Audience audience = sender.getSenderObject() instanceof Player player
                ? audiences.player(player)
                : sender.getSenderObject() instanceof CommandSender commandSender
                ? audiences.sender(commandSender)
                : null;


        if (audience == null) {
            return;
        }


        final Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        final Title title = Title.title(toComponent(titleStr), toComponent(subtitleStr), times);

        audience.showTitle(title);
    }

    @Override
    public void playSound(de.rayzs.pat.utils.sender.CommandSender sender, String soundKey, float volume, float pitch) throws Exception {
        if (sender.getSenderObject() instanceof Player player) {
            Sound sound = Sound.valueOf(soundKey);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
