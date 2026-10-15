package de.rayzs.pat.utils.message.translators;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.utils.message.*;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class VelocityMessageTranslator implements Translator {

    @Override
    public String strip(String text) {
        return miniMessage.stripTags(text);
    }

    @Override
    public void send(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
        if (sender.getSenderObject() instanceof CommandSource source) {
            source.sendMessage(toComponent(text));
        }
    }

    @Override
    public void sendActionbar(de.rayzs.pat.utils.sender.CommandSender sender, String text) {
        if (sender.getSenderObject() instanceof CommandSource source) {
            source.sendActionBar(toComponent(text));
        }
    }

    @Override
    public void sendTitle(de.rayzs.pat.utils.sender.CommandSender sender, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        final Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        final Title title = Title.title(toComponent(titleStr), toComponent(subtitleStr), times);

        if (sender.getSenderObject() instanceof CommandSource source) {
            source.showTitle(title);
        }
    }

    @Override
    public void playSound(de.rayzs.pat.utils.sender.CommandSender sender, String soundKey, float volume, float pitch) throws Exception {
        if (sender.getSenderObject() instanceof CommandSource source) {
            final Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f);
            source.playSound(sound, Sound.Emitter.self());
        }
    }

    @Override
    public void close() { }
}
