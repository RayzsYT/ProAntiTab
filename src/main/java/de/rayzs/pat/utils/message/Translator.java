package de.rayzs.pat.utils.message;

import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.sender.CommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Translator {

    MiniMessage miniMessage = MiniMessage.miniMessage();

    String strip(String text);

    void send(CommandSender sender, String text);

    void sendActionbar(CommandSender sender, String text);

    void sendTitle(CommandSender sender, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void playSound(CommandSender sender, String soundKey, float volume, float pitch) throws Exception;

    default Component toComponent(String text) {
        text = StringUtils.replace(text, "§", "&");

        final Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        text = miniMessage.serialize(legacy).replace("\\", "");

        return miniMessage.deserialize(text);
    }

    void close();
}
