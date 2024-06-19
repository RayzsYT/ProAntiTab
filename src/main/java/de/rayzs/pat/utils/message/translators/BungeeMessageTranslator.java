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

    private BungeeAudiences audiences;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BungeeMessageTranslator() {
        audiences = BungeeAudiences.create(BungeeLoader.getPlugin());
    }

    @Override
    public String translate(String text) {
        return miniMessage.serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        boolean isPlayer = target instanceof ProxiedPlayer;
        Audience audience = isPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);
        audience.sendMessage((isPlayer ? LegacyComponentSerializer.legacyAmpersand() : this.miniMessage).deserialize(text));
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
