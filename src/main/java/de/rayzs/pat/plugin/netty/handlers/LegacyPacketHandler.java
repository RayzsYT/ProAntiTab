package de.rayzs.pat.plugin.netty.handlers;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.netty.PacketAnalyzer;
import de.rayzs.pat.plugin.netty.PacketHandler;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import de.rayzs.pat.utils.*;
import java.util.*;

public class LegacyPacketHandler implements PacketHandler {

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
        String input = PacketAnalyzer.getPlayerInput(player);

        if(input == null) {
            Logger.warning("Failed PacketAnalyze process! (#2)");
            return false;
        }

        boolean cancelsBeforeHand = false;

        if(input.startsWith("/")) {
            input = input.replace("/", "");
            if(input.contains(" ")) input = input.split(" ")[0];

            cancelsBeforeHand = Storage.isCommandBlockedPrecise(input) && !PermissionUtil.hasBypassPermission(player, input)
                    || Storage.isCommandBlocked(input) && !PermissionUtil.hasBypassPermission(player, input);
        }

        for (Field field : Reflection.getFields(packetObj)) {
            field.setAccessible(true);
            Object result = field.get(packetObj);

            if (!(result instanceof String[])) continue;
            List<String> newResultList = new ArrayList<>();
            String[] tR = (String[]) result;

            String tempName;

            if(!cancelsBeforeHand)
                for (String s : tR) {
                    tempName = s.replaceFirst("/", "");

                    if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
                        if(!Storage.isCommandBlockedPrecise(tempName)) continue;
                        if(PermissionUtil.hasBypassPermission(player, tempName)) continue;
                        newResultList.add(s);
                        continue;
                    }

                    if (tempName.contains(":")) tempName = tempName.split(":")[1];

                    if (!Storage.isCommandBlocked(tempName)
                            || Storage.isCommandBlocked(tempName)
                            && PermissionUtil.hasBypassPermission(player, tempName))
                        newResultList.add(s);
                }

            field.set(packetObj, newResultList.toArray(new String[0]));
        }

        return true;
    }
}
