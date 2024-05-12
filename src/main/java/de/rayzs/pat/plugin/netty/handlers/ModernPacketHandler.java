package de.rayzs.pat.plugin.netty.handlers;

import com.mojang.brigadier.suggestion.Suggestions;
import de.rayzs.pat.plugin.netty.PacketHandler;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import de.rayzs.pat.utils.*;

public class ModernPacketHandler implements PacketHandler {

    @Override
    public void handlePacket(Player player, Object packetObj) throws Exception {
        Field suggestionsField = Reflection.getFirstFieldByType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS);
        if(suggestionsField == null) return;

        Suggestions suggestions = (Suggestions) suggestionsField.get(packetObj);
        suggestions.getList().removeIf(suggestion -> {
            String command = suggestion.getText();
            if(Storage.TURN_BLACKLIST_TO_WHITELIST)
                return !Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
            else return Storage.isCommandBlocked(command) && !PermissionUtil.hasBypassPermission(player, command);
        });
    }
}
