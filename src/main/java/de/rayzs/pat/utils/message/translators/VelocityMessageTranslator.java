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
    public String translate(String text) {
        return MiniMessage.miniMessage().serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        if (target instanceof CommandSource source) {
            source.sendMessage(toComponent(text));
        }
    }

    @Override
    public void sendActionbar(Object target, String text) {
        if (target instanceof CommandSource source) {
            source.sendActionBar(toComponent(text));
        }
    }

    @Override
    public void sendTitle(Object target, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        Title title = Title.title(toComponent(titleStr), toComponent(subtitleStr), times);

        if (target instanceof CommandSource source) {
            source.showTitle(title);
        }
    }

    @Override
    public void playSound(Object target, String soundKey, float volume, float pitch) throws Exception {
        if (target instanceof CommandSource source) {
            Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f);

            source.playSound(sound, Sound.Emitter.self());
        }
    }

    @Override
    public void close() { }
}
