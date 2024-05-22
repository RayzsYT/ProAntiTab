package de.rayzs.pat.api.netty.handlers;

import com.mojang.brigadier.suggestion.Suggestions;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.netty.*;
import org.bukkit.entity.Player;
import java.lang.reflect.*;
import de.rayzs.pat.utils.*;

public class ModernPacketHandler implements PacketHandler {

    @Override
    public boolean handleIncomingPacket(Player player, Object packetObj) throws Exception {
        Field stringField = Reflection.getFirstFieldByType(packetObj.getClass(), "String", Reflection.SearchOption.ENDS);
        if(stringField == null) {
            Logger.warning("Failed PacketAnalyze process! (#1)");
            return false;
        }

        String text = (String) stringField.get(packetObj);
        PacketAnalyzer.insertPlayerInput(player, text);
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(Player player, Object packetObj) throws Exception {
        Object suggestionObj;

        if (Reflection.getMinor() < 17) {
            Field suggestionsField = Reflection.getFieldsByType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS).get(0);
            if (suggestionsField == null) return false;
            suggestionObj = suggestionsField.get(packetObj);
        } else {
            Method suggestionsMethod = Reflection.getMethodsByReturnType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS).get(0);
            if (suggestionsMethod == null) return false;
            suggestionObj = suggestionsMethod.invoke(packetObj);
        }

        String input = PacketAnalyzer.getPlayerInput(player);

        if(input == null) {
            Logger.warning("Failed PacketAnalyze process! (#2)");
            return false;
        }

        boolean cancelsBeforeHand = false;
        if(input.startsWith("/")) {
            input = input.replace("/", "");
            String[] split = input.split(" ");
            if(input.contains(" ")) input = split[0];

            cancelsBeforeHand = Storage.Blacklist.isListed(input, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
        }

        Suggestions suggestions = (Suggestions) suggestionObj;
        if(input.length() < 1 && Reflection.isWeird() || cancelsBeforeHand && Reflection.isWeird())
            return false;
        else if(cancelsBeforeHand)
            suggestions.getList().clear();
        else {
            if(Reflection.isWeird()) return true;

            suggestions.getList().removeIf(suggestion -> {
                String command = suggestion.getText();
                return Storage.Blacklist.isBlocked(player, command);
            });
        }


        return true;
    }
}