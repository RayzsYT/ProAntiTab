package de.rayzs.pat.utils.message.translators;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.audience.Audience;
import de.rayzs.pat.plugin.BukkitLoader;
import net.kyori.adventure.title.Title;
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
    public String translate(String text) {
        return miniMessage.serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        Audience audience = target instanceof Player ? audiences.player((Player) target) : audiences.sender((CommandSender) target);

        audience.sendMessage(toComponent(text));
    }

    @Override
    public void sendActionbar(Object target, String text) {
        Audience audience = target instanceof Player ? audiences.player((Player) target) : audiences.sender((CommandSender) target);

        audience.sendActionBar(toComponent(text));
    }

    @Override
    public void sendTitle(Object target, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        Audience audience = target instanceof Player ? audiences.player((Player) target) : audiences.sender((CommandSender) target);

        Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        Title title = Title.title(toComponent(titleStr), toComponent(subtitleStr), times);

        audience.showTitle(title);
    }

    @Override
    public void playSound(Object target, String soundKey, float volume, float pitch) throws Exception {
        if (target instanceof Player player) {
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
