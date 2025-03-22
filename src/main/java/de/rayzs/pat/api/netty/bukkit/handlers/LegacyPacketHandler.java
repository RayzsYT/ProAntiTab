package de.rayzs.pat.api.netty.bukkit.handlers;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.netty.bukkit.*;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import de.rayzs.pat.utils.*;
import java.util.*;
import java.util.stream.Collectors;

public class LegacyPacketHandler implements BukkitPacketHandler {

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
        String input = BukkitPacketAnalyzer.getPlayerInput(player);

        if(input == null) return false;

        boolean cancelsBeforeHand;
        int spaces = 0;

        if(input.startsWith("/")) {
            input = input.replace("/", "");
            if(input.contains(" ")) {
                String[] split = input.split(" ");
                spaces = split.length;
                if(spaces > 0) input = split[0];
            }

            cancelsBeforeHand = Storage.Blacklist.isBlocked(player, input, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
            if (!cancelsBeforeHand) cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(input) || Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(input);
        } else return true;

        for (Field field : Reflection.getFields(packetObj)) {
            field.setAccessible(true);
            Object result = field.get(packetObj);

            if (!(result instanceof String[])) continue;
            List<String> newResultList = new ArrayList<>();
            String[] tR = (String[]) result;

            String tempName;

            boolean doesBypassNamespace = Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player);

            if(spaces == 0) {
                for (String s : tR) {
                    if(!BukkitLoader.isLoaded()) continue;
                    if(!s.startsWith("/")) {
                        newResultList.add(s);
                        continue;
                    }

                    tempName = s.replaceFirst("/", "");

                    if (!doesBypassNamespace && Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(tempName))
                        continue;

                    if (!Storage.Blacklist.isBlocked(player, tempName, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED))
                        newResultList.add(s);
                }
            } else {
                if(!cancelsBeforeHand) {
                    FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), input, Arrays.asList(tR));
                    if(!filteredTabCompletionEvent.isCancelled()) {
                        List<String> suggestions = Arrays.stream(tR).filter(s -> filteredTabCompletionEvent.getCompletion().contains(s)).collect(Collectors.toList());
                        newResultList.addAll(suggestions);
                    }
                }
            }

            field.set(packetObj, newResultList.toArray(new String[0]));
        }

        return true;
    }
}
