package de.rayzs.pat.api.netty.bukkit.handlers;

import com.mojang.brigadier.suggestion.Suggestions;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.netty.bukkit.*;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import java.lang.reflect.*;

public class ModernPacketHandler implements BukkitPacketHandler {

    @Override
    public boolean handleIncomingPacket(Player player, Object packetObj) throws Exception {
        Field stringField = Reflection.getFirstFieldByType(packetObj.getClass(), "String", Reflection.SearchOption.ENDS);

        if(stringField == null) {
            Logger.warning("Failed PacketAnalyze process! (#1)");
            return false;
        }

        String text = (String) stringField.get(packetObj);
        BukkitPacketAnalyzer.insertPlayerInput(player, text);
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(Player player, Object packetObj) throws Exception {
        Object suggestionObj;

        String input = BukkitPacketAnalyzer.getPlayerInput(player);
        if(input == null) return false;

        boolean cancelsBeforeHand = false;
        int spaces = 0;
        if(input.startsWith("/")) {
            input = input.replace("/", "");
            if(input.contains(" ")) {
                String[] split = input.split(" ");
                spaces = split.length;
                if(spaces > 0) input = split[0];
            }

            cancelsBeforeHand = Storage.Blacklist.isBlocked(player, input, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
            if(!cancelsBeforeHand) cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(input);
        }

        if(packetObj.getClass().getSimpleName().equals("ClientboundCommandSuggestionsPacket")) {
            return !cancelsBeforeHand;
        }

        if (Reflection.getMinor() < 17) {
            Field suggestionsField = Reflection.getFieldsByType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS).get(0);
            if (suggestionsField == null) return false;
            suggestionObj = suggestionsField.get(packetObj);
        } else {
            Method suggestionsMethod = Reflection.getMethodsByReturnType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS).get(0);
            if (suggestionsMethod == null) return false;
            suggestionObj = suggestionsMethod.invoke(packetObj);
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