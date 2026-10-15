package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.utils.message.Translator;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

public class PaperMessageTranslator implements Translator {

    @Override
    public String strip(String text) {
        return ChatColor.stripColor(miniMessage.stripTags(text));
    }

    @Override
    public void send(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
            if (sender.getSenderObject() instanceof Player player) {
                player.sendMessage(toComponent(text));
            } else if (sender.getSenderObject() instanceof CommandSender commandSender) {
                commandSender.sendMessage(toComponent(text));
            }
    }

    @Override
    public void sendActionbar(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
        if (sender.getSenderObject() instanceof Player player) {
            player.sendActionBar(toComponent(text));
        } else if (sender.getSenderObject() instanceof CommandSender commandSender) {
            commandSender.sendActionBar(toComponent(text));
        }
    }

    @Override
    public void sendTitle(de.rayzs.pat.utils.sender.CommandSender sender, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        final Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        final Title title = Title.title(toComponent(titleStr), toComponent(subtitleStr), times);

        if (sender.getSenderObject() instanceof Player player) {
            player.showTitle(title);
        } else if (sender.getSenderObject() instanceof CommandSender commandSender) {
            commandSender.showTitle(title);
        }
    }

    @Override
    public void playSound(de.rayzs.pat.utils.sender.CommandSender sender, String soundKey, float volume, float pitch) throws Exception {
        if (sender.getSenderObject() instanceof Player player) {
            final Sound sound = Sound.valueOf(soundKey);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    @Override
    public void close() {}
}
