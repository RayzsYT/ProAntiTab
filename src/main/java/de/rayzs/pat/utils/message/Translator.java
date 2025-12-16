package de.rayzs.pat.utils.message;

import de.rayzs.pat.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Translator {

    MiniMessage miniMessage = MiniMessage.miniMessage();

    String translate(String text);

    void send(Object target, String text);

    void sendActionbar(Object target, String text);

    void sendTitle(Object target, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void playSound(Object target, String soundKey, float volume, float pitch) throws Exception;

    default Component toComponent(String text) {
        text = StringUtils.replace(text, "§", "&");

        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        text = StringUtils.replace(miniMessage.serialize(legacy), "\\", "");

        return miniMessage.deserialize(text);
    }

    void close();
}
