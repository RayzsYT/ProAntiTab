package de.rayzs.pat.utils.message.translators;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.kyori.adventure.audience.Audience;
import de.rayzs.pat.utils.message.Translator;
import net.md_5.bungee.api.CommandSender;
import de.rayzs.pat.plugin.BungeeLoader;
import org.bukkit.ChatColor;

import java.time.Duration;

public class BungeeMessageTranslator implements Translator {

    private BungeeAudiences audiences;

    public BungeeMessageTranslator() {
        audiences = BungeeAudiences.create(BungeeLoader.getPlugin());
    }

    @Override
    public String strip(String text) {
        return ChatColor.stripColor(miniMessage.stripTags(text));
    }

    @Override
    public void send(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
        final Audience audience = sender.getSenderObject() instanceof ProxiedPlayer player
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
        final Audience audience = sender.getSenderObject() instanceof ProxiedPlayer player
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
        final Audience audience = sender.getSenderObject() instanceof ProxiedPlayer player
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
        final Audience audience = sender.getSenderObject() instanceof ProxiedPlayer player
                ? audiences.player(player)
                : sender.getSenderObject() instanceof CommandSender commandSender
                ? audiences.sender(commandSender)
                : null;


        if (audience == null) {
            return;
        }


        final Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f);
        audience.playSound(sound, Sound.Emitter.self());
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
