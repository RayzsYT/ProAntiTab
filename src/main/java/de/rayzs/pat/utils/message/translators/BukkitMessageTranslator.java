package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.message.Translator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitMessageTranslator implements Translator {

    private static BukkitAudiences audiences;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void setAudience(AudienceProvider provider) {
        audiences = (BukkitAudiences) provider;
    }

    @Override
    public void send(Object target, String text) {
        Audience audience = target instanceof Player ? audiences.player((Player) target) : audiences.sender((CommandSender) target);
        audience.sendMessage(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }
}
