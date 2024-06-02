package de.rayzs.pat.api.netty.bukkit.handlers;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.netty.bukkit.PacketAnalyzer;
import de.rayzs.pat.api.netty.bukkit.PacketHandler;
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

        if(input == null) return false;

        boolean cancelsBeforeHand = false;
        int spaces = 0;

        if(input.startsWith("/")) {
            input = input.replace("/", "");
            if(input.contains(" ")) {
                String[] split = input.split(" ");
                input = split[0];
                spaces = split.length;
            }

            cancelsBeforeHand = Storage.Blacklist.isBlocked(player, input, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
        } else return true;

        for (Field field : Reflection.getFields(packetObj)) {
            field.setAccessible(true);
            Object result = field.get(packetObj);

            if (!(result instanceof String[])) continue;
            List<String> newResultList = new ArrayList<>();
            String[] tR = (String[]) result;

            String tempName;

            if(spaces == 0) {
                for (String s : tR) {
                    if(!BukkitLoader.isLoaded()) continue;
                    if(!s.startsWith("/")) {
                        newResultList.add(s);
                        continue;
                    }

                    tempName = s.replaceFirst("/", "");

                    if (!Storage.Blacklist.isBlocked(player, tempName, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED))
                        newResultList.add(s);
                }
            } else {
                if(!cancelsBeforeHand) newResultList.addAll(Arrays.asList(tR));
            }

            field.set(packetObj, newResultList.toArray(new String[0]));
        }

        return true;
    }
}
