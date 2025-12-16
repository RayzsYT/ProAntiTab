package de.rayzs.pat.utils.message.translators;

import de.rayzs.pat.utils.StringUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.proxy.*;
import de.rayzs.pat.utils.message.*;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class VelocityMessageTranslator implements Translator {

    @Override
    public String translate(String text) {
        return MiniMessage.miniMessage().serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        text = fixLegacy(StringUtils.replace(text, "\\", ""));

        Component component = miniMessage.deserialize(text);

        if (target instanceof CommandSource source) {
            source.sendMessage(component);
        }
    }

    @Override
    public void sendActionbar(Object target, String text) {
        text = fixLegacy(StringUtils.replace(text, "\\", ""));

        Component component = miniMessage.deserialize(text);

        System.out.println("Actionbar try");

        if (target instanceof CommandSource source) {
            System.out.println("Actionbar for source");
            source.sendActionBar(component);
        }
    }

    @Override
    public void sendTitle(Object target, String titleStr, String subtitleStr, int fadeIn, int stay, int fadeOut) {
        titleStr = fixLegacy(StringUtils.replace(titleStr, "\\", ""));
        subtitleStr = fixLegacy(StringUtils.replace(subtitleStr, "\\", ""));

        Component titleComponent = miniMessage.deserialize(titleStr);
        Component subtitleComponent = miniMessage.deserialize(subtitleStr);

        Title title = Title.title(titleComponent, subtitleComponent, fadeIn, stay, fadeOut);

        if (target instanceof CommandSource source) {
            source.showTitle(title);
        }
    }

    @Override
    public void close() { }
}
