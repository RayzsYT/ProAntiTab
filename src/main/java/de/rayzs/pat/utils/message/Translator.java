package de.rayzs.pat.utils.message;

import de.rayzs.pat.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Translator {

    MiniMessage miniMessage = MiniMessage.miniMessage();

    String translate(String text);

    void send(Object target, String text);

    void sendActionbar(Object target, String text);

    void sendTitle(Object target, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    default String fixLegacy(String text) {
        text = StringUtils.replace(text, "§", "&");

        TextComponent textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(text);

        return miniMessage.serialize(textComponent);

        // return textComponent.content(); Allows it, but removes all legacy text.
    }

    void close();
}
