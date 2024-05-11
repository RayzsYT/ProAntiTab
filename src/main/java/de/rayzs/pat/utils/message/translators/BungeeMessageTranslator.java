package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.message.Translator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeMessageTranslator implements Translator {

    private final BungeeAudiences audiences;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BungeeMessageTranslator() {
        audiences = BungeeAudiences.create(BungeeLoader.getPlugin());
    }

    @Override
    public void send(Object target, String text) {
        System.out.println("Penis");
        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);
        audience.sendMessage(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }
}
