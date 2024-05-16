package de.rayzs.pat.plugin.netty.handlers;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.netty.PacketAnalyzer;
import de.rayzs.pat.plugin.netty.PacketHandler;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.rayzs.pat.utils.*;

public class ModernPacketHandler implements PacketHandler {

    /*
    https://haste.rayzs.de/fapunebeva.md
     */

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
        Method suggestionsMethod = Reflection.getMethodsByReturnType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS).get(0);
        if(suggestionsMethod == null) return false;

        String input = PacketAnalyzer.getPlayerInput(player);

        if(input == null) {
            Logger.warning("Failed PacketAnalyze process! (#2)");
            return false;
        }

        boolean cancelsBeforeHand = false,
                // 1.20.6 support coming soon bcs it's to annoying to work with on packet-base rn
                weird = Reflection.getMinor() == 20 && Reflection.getRelease() >= 6 || Reflection.getMinor() >= 20;

        if(input.startsWith("/")) {
            input = input.replace("/", "");
            String[] split = input.split(" ");
            if(input.contains(" ")) input = split[0];

            cancelsBeforeHand = !Storage.TURN_BLACKLIST_TO_WHITELIST && Storage.isCommandBlockedPrecise(input) && !PermissionUtil.hasBypassPermission(player, input)
                    || Storage.TURN_BLACKLIST_TO_WHITELIST && !Storage.isCommandBlocked(input) && !PermissionUtil.hasBypassPermission(player, input);
        }

        Suggestions suggestions = (Suggestions) suggestionsMethod.invoke(packetObj);
        if(input.length() < 1 && weird || cancelsBeforeHand && weird)
            return false;
        else if(cancelsBeforeHand)
            suggestions.getList().clear();
        else {
            if(weird) return true;

            suggestions.getList().removeIf(suggestion -> {
                String command = suggestion.getText();
                if (Storage.TURN_BLACKLIST_TO_WHITELIST)
                    return !Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
                else return Storage.isCommandBlocked(command) && !PermissionUtil.hasBypassPermission(player, command);
            });
        }


        return true;
    }
}