package de.rayzs.pat.api.netty.handlers;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.netty.PacketAnalyzer;
import de.rayzs.pat.api.netty.PacketHandler;
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

            cancelsBeforeHand = Storage.Blacklist.isListed(input, true) && !PermissionUtil.hasBypassPermission(player, input)
                    || Storage.Blacklist.isListed(input, false) && !PermissionUtil.hasBypassPermission(player, input);
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

                    if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                        if(!Storage.Blacklist.isListed(tempName, true)) continue;
                        if(PermissionUtil.hasBypassPermission(player, tempName)) continue;
                        newResultList.add(s);
                        continue;
                    }

                    if (tempName.contains(":")) tempName = tempName.split(":")[1];

                    if (!Storage.Blacklist.isListed(tempName, false)
                            || Storage.Blacklist.isListed(tempName, false)
                            && PermissionUtil.hasBypassPermission(player, tempName))
                        newResultList.add(s);
                }

            field.set(packetObj, newResultList.toArray(new String[0]));
        }

        return true;
    }
}
