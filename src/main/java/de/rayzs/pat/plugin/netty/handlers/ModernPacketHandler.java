package de.rayzs.pat.plugin.netty.handlers;

import com.mojang.brigadier.suggestion.Suggestions;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.netty.PacketHandler;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
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
        playerInputCache.put(player, text);
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(Player player, Object packetObj) throws Exception {
        Field suggestionsField = Reflection.getFirstFieldByType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS);
        if(suggestionsField == null) return false;

        String input = playerInputCache.get(player);

        if(input == null) {
            Logger.warning("Failed PacketAnalyze process! (#2)");
            return false;
        }

        playerInputCache.remove(player);

        Suggestions suggestions = (Suggestions) suggestionsField.get(packetObj);
        suggestions.getList().removeIf(suggestion -> {
            String command = suggestion.getText();
            if(Storage.TURN_BLACKLIST_TO_WHITELIST)
                return !Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
            else return Storage.isCommandBlocked(command) && !PermissionUtil.hasBypassPermission(player, command);
        });

        return true;
    }
}
