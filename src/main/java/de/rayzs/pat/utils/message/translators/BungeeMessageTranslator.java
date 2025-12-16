package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.utils.StringUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.kyori.adventure.audience.Audience;
import de.rayzs.pat.utils.message.Translator;
import net.md_5.bungee.api.CommandSender;
import de.rayzs.pat.plugin.BungeeLoader;

import java.time.Duration;

public class BungeeMessageTranslator implements Translator {

    private BungeeAudiences audiences;

    public BungeeMessageTranslator() {
        audiences = BungeeAudiences.create(BungeeLoader.getPlugin());
    }

    @Override
    public String translate(String text) {
        return miniMessage.serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);

        audience.sendMessage(toComponent(text));
    }

    @Override
    public void sendActionbar(Object target, String text) {
        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);

        audience.sendActionBar(toComponent(text));
    }

    @Override
    public void sendTitle(Object target, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);

        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        Title title = Title.title(toComponent(titleStr), toComponent(subtitleStr), times);

        audience.showTitle(title);
    }

    @Override
    public void playSound(Object target, String soundKey, float volume, float pitch) throws Exception {
        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);
        Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f);

        audience.playSound(sound, Sound.Emitter.self());
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
