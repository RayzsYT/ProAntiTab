package de.rayzs.pat.api.netty.bukkit.handlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;

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
        final String rawInput = BukkitPacketAnalyzer.getPlayerInput(player);

        if(rawInput == null)
            return false;

        String input = rawInput;
        boolean cancelsBeforeHand = false;

        if (!input.startsWith("/")) {
            return true;
        }

        input = input.substring(1);

        final boolean doesBypassNamespace = Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player);
        final boolean spaces = input.contains(" ");

        if (Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(input) && !doesBypassNamespace) {
            cancelsBeforeHand = true;
        }

        if (!cancelsBeforeHand && !input.isEmpty()) {
            cancelsBeforeHand = !Storage.Blacklist.canPlayerAccessTab(player, StringUtils.getFirstArg(input));
        }

        if (!cancelsBeforeHand) {
            cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(input) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(input);
        }

        for (Field field : Reflection.getFields(packetObj)) {
            field.setAccessible(true);
            Object result = field.get(packetObj);

            if (!(result instanceof String[])) {
                continue;
            }

            String[] tR = (String[]) result;
            List<String> suggestions = new ArrayList<>(Arrays.asList(tR));

            if (spaces) {

                if (cancelsBeforeHand) {
                    suggestions.clear();
                    field.set(packetObj, suggestions.toArray(new String[0]));
                    return true;
                }

                FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), rawInput, new ArrayList<>(suggestions));

                if (filteredTabCompletionEvent.isCancelled()) {
                    suggestions.clear();
                } else {
                    suggestions.removeIf(s -> !filteredTabCompletionEvent.getCompletion().contains(s));
                }

            } else {
                suggestions.removeIf(s -> {
                    String cpy = s;
                    if (cpy.startsWith("/")) {
                        cpy = cpy.substring(1);
                    }

                    if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(cpy) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(cpy)) {
                        return false;
                    }

                    return !Storage.Blacklist.canPlayerAccessTab(player, cpy);
                });
            }

            field.set(packetObj, suggestions.toArray(new String[0]));
        }

        return true;
    }
}
