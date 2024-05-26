package de.rayzs.pat.api.netty.handlers;

import com.mojang.brigadier.suggestion.Suggestions;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
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

        if(input == null) return false;

        boolean cancelsBeforeHand = false;
        int spaces = 0;
        if(input.startsWith("/")) {
            input = input.replace("/", "");
            String[] split = input.split(" ");
            if(input.contains(" ")) {
                spaces = split.length;
                input = split[0];
            }

            cancelsBeforeHand = Storage.Blacklist.isBlocked(player, input, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
        }

        Suggestions suggestions = (Suggestions) suggestionObj;
        if((input.length() < 1 || cancelsBeforeHand) && Reflection.isWeird()) return false;

        if(spaces >= 1 && cancelsBeforeHand || !BukkitLoader.isLoaded()) {
            suggestions.getList().clear();
            return true;
        }

        if(spaces == 0) {
            suggestions.getList().removeIf(suggestion -> {
                String command = suggestion.getText();
                return Storage.Blacklist.isBlocked(player, command);
            });
            return true;
        }

        return true;
    }
}