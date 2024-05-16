package de.rayzs.pat.utils.message.translators;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.*;
import de.rayzs.pat.utils.message.*;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class VelocityMessageTranslator implements Translator {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public String translate(String text) {
        return miniMessage.serialize(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void send(Object target, String text) {
        if(target instanceof Player)
            ((Player) target).sendMessage(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
        else if(target instanceof CommandSource)
            ((CommandSource) target).sendMessage(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
        else if(target instanceof ConsoleCommandSource)
            ((ConsoleCommandSource) target).sendMessage(miniMessage.deserialize(MessageTranslator.translateLegacy(text)));
    }

    @Override
    public void close() { }
}
