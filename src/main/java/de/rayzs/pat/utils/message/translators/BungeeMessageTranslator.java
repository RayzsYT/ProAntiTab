package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.kyori.adventure.audience.Audience;
import de.rayzs.pat.utils.message.Translator;
import net.md_5.bungee.api.CommandSender;
import de.rayzs.pat.plugin.BungeeLoader;

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
        text = StringUtils.replace(text, "&", "§", "\\", "");

        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);
        Component component = miniMessage.deserialize(text);

        audience.sendMessage(component);
    }

    @Override
    public void sendActionbar(Object target, String text) {
        text = StringUtils.replace(text, "&", "§", "\\", "");

        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);
        Component component = miniMessage.deserialize(text);

        audience.sendActionBar(component);
    }

    @Override
    public void sendTitle(Object target, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        titleStr = StringUtils.replace(titleStr, "&", "§", "\\", "");
        subtitleStr = StringUtils.replace(subtitleStr, "&", "§", "\\", "");

        Audience audience = target instanceof ProxiedPlayer ? audiences.player((ProxiedPlayer) target) : audiences.sender((CommandSender) target);
        Component titleComponent = miniMessage.deserialize(titleStr);
        Component subtitleComponent = miniMessage.deserialize(subtitleStr);

        Title title = Title.title(titleComponent, subtitleComponent, fadeIn, stay, fadeOut);

        audience.showTitle(title);
    }

    @Override
    public void close() {
        audiences.close();
        audiences = null;
    }
}
