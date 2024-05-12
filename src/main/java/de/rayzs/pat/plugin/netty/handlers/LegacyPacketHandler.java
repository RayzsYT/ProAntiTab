package de.rayzs.pat.plugin.netty.handlers;

import de.rayzs.pat.plugin.netty.PacketHandler;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import de.rayzs.pat.utils.*;
import java.util.*;

public class LegacyPacketHandler implements PacketHandler {

    @Override
    public void handlePacket(Player player, Object packetObj) throws Exception {
        for (Field field : Reflection.getFields(packetObj)) {
            field.setAccessible(true);
            Object result = field.get(packetObj);

            if (!(result instanceof String[])) continue;
            List<String> newResultList = new ArrayList<>();
            String[] tR = (String[]) result;

            String tempName;

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
    }
}
