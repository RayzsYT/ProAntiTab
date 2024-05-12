package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.message.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitMessageTranslator implements Translator {

    private BukkitAudiences audiences;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BukkitMessageTranslator() {
        audiences = BukkitAudiences.create(BukkitLoader.getPlugin());
    }

    @Override
    public void send(Object target, String text) {
        Audience audience = target instanceof Player ? audiences.player((Player) target) : audiences.sender((CommandSender) target);
        audience.sendMessage(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
