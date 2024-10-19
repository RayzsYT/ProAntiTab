package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.message.Translator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeMessageTranslator implements Translator {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
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
        text = this.miniMessage.serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(text.replace("§", "&")));
        if (text.contains("\\")) text = text.replace("\\", "");
        audience.sendMessage(this.miniMessage.deserialize(text));
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
