package de.rayzs.pat.utils.message.translators;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.*;
import de.rayzs.pat.utils.message.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityMessageTranslator implements Translator {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public String translate(String text) {
        return miniMessage.serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        text = this.miniMessage.serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(text.replace("§", "&")));
        if(text.contains("\\")) text = text.replace("\\", "");
        final Component component = this.miniMessage.deserialize(text);

        if(target instanceof Player)
            ((Player) target).sendMessage(component);
        else if(target instanceof CommandSource)
            ((ConsoleCommandSource) target).sendMessage(component);
        else if(target instanceof ConsoleCommandSource)
            ((ConsoleCommandSource) target).sendMessage(component);
    }

    @Override
    public void close() { }
}
