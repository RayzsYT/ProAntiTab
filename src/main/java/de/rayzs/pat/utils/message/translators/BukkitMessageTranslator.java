package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.message.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitMessageTranslator implements Translator {

    private BukkitAudiences audiences;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

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
        text = this.miniMessage.serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(text.replace("§", "&")));
        if(text.contains("\\")) text = text.replace("\\", "");
        audience.sendMessage(this.miniMessage.deserialize(text));
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
