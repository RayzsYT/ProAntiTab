package de.rayzs.pat.api.netty.bukkit.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.scheduler.PATScheduler;

public class ModernPacketHandler implements BukkitPacketHandler {

    private static boolean modifiable = true;

    @Override
    public boolean handleIncomingPacket(Player player, Object packetObj) throws Exception {
        Field stringField = Reflection.getFirstFieldByType(packetObj.getClass(), "String", Reflection.SearchOption.ENDS);

        if(stringField == null) {
            Logger.warning("Failed PacketAnalyze process! (#1)");
            return false;
        }

        String text = (String) stringField.get(packetObj);
        if (Storage.ConfigSections.Settings.PATCH_EXPLOITS.isMalicious(text)) {
            MessageTranslator.send(
                    Bukkit.getConsoleSender(),
                    Storage.ConfigSections.Settings.PATCH_EXPLOITS.ALERT_MESSAGE.get().replace("%player%", player.getName())
            );

            PATScheduler.createScheduler(() ->
                    player.kickPlayer(
                            StringUtils.replace(Storage.ConfigSections.Settings.PATCH_EXPLOITS.KICK_MESSAGE.get(), "&", "§")
                    )
            );

            return false;
        }

        BukkitPacketAnalyzer.insertPlayerInput(player, text);
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(Player player, Object packetObj) throws Exception {
        Object suggestionObj;
        String rawInput = BukkitPacketAnalyzer.getPlayerInput(player), input = rawInput;

        if(input == null)
            return false;


        boolean is121Packet = packetObj.getClass().getSimpleName().equals("ClientboundCommandSuggestionsPacket"),
                cancelsBeforeHand = false;

        int spaces = 0;
        if(input.startsWith("/") || is121Packet) {
            input = input.replace("/", "");
            if(input.contains(" ")) {
                String[] split = input.split(" ");
                spaces = split.length;
                if(spaces > 0) input = split[0];
            }

            cancelsBeforeHand = !Storage.Blacklist.canPlayerAccessTab(player, input);

            if (!cancelsBeforeHand)
                cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(input) || Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(input);
        }

        if (is121Packet || Reflection.isFoliaServer() && packetObj.getClass().getSimpleName().equals("PacketPlayOutTabComplete")) {
            try {
                Field suggestionsField = Reflection.getFieldsByTypeNormal(packetObj.getClass(), "List", Reflection.SearchOption.ENDS).get(0);
                List<?> suggestionsTmp = (List<?>) suggestionsField.get(packetObj),
                suggestions = new ArrayList<>(suggestionsTmp);

                List<Field> intFields = Reflection.getFieldsByTypeNormal(packetObj.getClass(), "int", Reflection.SearchOption.ENDS);
                int id = (int) intFields.get(0).get(packetObj),
                        start = (int) intFields.get(1).get(packetObj),
                        length = (int) intFields.get(2).get(packetObj);

                Class<?> clientboundCommandSuggestionsPacketClass = Class.forName("net.minecraft.network.protocol.game."  + (packetObj.getClass().getSimpleName().equals("PacketPlayOutTabComplete") ? "PacketPlayOutTabComplete" : "ClientboundCommandSuggestionsPacket"));
                if ((input.isEmpty() || cancelsBeforeHand) && Reflection.isWeird())
                    return false;

                if (spaces >= 1 && cancelsBeforeHand || !BukkitLoader.isLoaded()) {
                    suggestions.clear();
                    return true;
                }

                if (spaces == 0) {
                    suggestions.removeIf(suggestion -> {
                        String command = getSuggestionFromEntry(suggestion);
                        return !Storage.Blacklist.canPlayerAccessTab(player, command);
                    });

                    return true;

                } else {
                    List<String> suggestionsAsString = new ArrayList<>();
                    for (Object suggestion : suggestions)
                        suggestionsAsString.add(getSuggestionFromEntry(suggestion));

                    FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), rawInput, suggestionsAsString);
                    if (filteredTabCompletionEvent.isCancelled()) suggestions.clear();

                    for (int i = 0; i < suggestions.size(); i++) {
                        Object csO = suggestions.get(i);

                        String suggestionAsString = getSuggestionFromEntry(csO);
                        if (!filteredTabCompletionEvent.getCompletion().contains(suggestionAsString))
                            suggestions.remove(csO);
                    }

                    suggestions.removeIf(suggestion -> !filteredTabCompletionEvent.getCompletion().contains(getSuggestionFromEntry(suggestion)));
                }

                if(!suggestions.isEmpty()) {

                    Object clientboundCommandSuggestionsPacketObj = clientboundCommandSuggestionsPacketClass
                            .getConstructor(int.class, int.class, int.class, List.class)
                            .newInstance(id, start, length, suggestions);

                    BukkitPacketAnalyzer.sendPacket(player.getUniqueId(), clientboundCommandSuggestionsPacketObj);
                }

                return false;

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return !cancelsBeforeHand;
        }


        if (Reflection.getMinor() < 17) {
            Field suggestionsField = Reflection.getFieldsByType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS).get(0);
            if (suggestionsField == null) return false;
            suggestionObj = suggestionsField.get(packetObj);
        } else {
            List<Method> methods = Reflection.getMethodsByReturnType(packetObj.getClass(), "Suggestions", Reflection.SearchOption.ENDS);

            if (methods.isEmpty()) return false;
            Method suggestionsMethod = methods.get(0);

            if (suggestionsMethod == null)
                return false;

            suggestionObj = suggestionsMethod.invoke(packetObj);
        }

        Suggestions suggestions = (Suggestions) suggestionObj;

        if((input.isEmpty() || cancelsBeforeHand) && Reflection.isWeird()) {
            return false;
        }

        if(spaces >= 1 && cancelsBeforeHand || !BukkitLoader.isLoaded()) {
            suggestions.getList().clear();
            return true;
        }

        if(spaces == 0) {

            suggestions.getList().removeIf(suggestion -> {
                String command = suggestion.getText();
                return !Storage.Blacklist.canPlayerAccessTab(player, command);
            });

            return true;

        } else {
            List<String> suggestionsAsString = new ArrayList<>();
            for (Suggestion suggestion : suggestions.getList())
                suggestionsAsString.add(suggestion.getText());

            FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), rawInput, suggestionsAsString);

            if (modifiable) {

                try {

                    if (filteredTabCompletionEvent.isCancelled())
                        suggestions.getList().clear();
                    else
                        suggestions.getList().removeIf(suggestion -> !filteredTabCompletionEvent.getCompletion().contains(suggestion.getText()));

                    return !suggestions.getList().isEmpty();

                } catch (UnsupportedOperationException exception) {
                    modifiable = false;
                }
            }

            return handleUnmodifiableTabCompletion(player, packetObj, suggestions, filteredTabCompletionEvent);
        }
    }

    private boolean handleUnmodifiableTabCompletion(Player player, Object packetObj, Suggestions suggestions, FilteredTabCompletionEvent filteredTabCompletionEvent) {
        List<Suggestion> filteredSuggestionsList = new ArrayList<>();

        if (!filteredTabCompletionEvent.isCancelled())
            for (Suggestion suggestion : suggestions.getList()) {
                if (filteredTabCompletionEvent.getCompletion().contains(suggestion.getText())) {
                    filteredSuggestionsList.add(suggestion);
                }
            }

        Suggestions filteredSuggestions = new Suggestions(suggestions.getRange(), filteredSuggestionsList);

        try {
            int val = -1;

            Method method = Reflection.getMethodByName(packetObj.getClass(), "e");
            if (method != null) {
                val = (int) method.invoke(packetObj);
            }

            if (val == -1) {
                Logger.warning("Failed to read packet input!");
                return false;
            }

            Object packet = Reflection.getConstructor(packetObj.getClass(), int.class, Suggestions.class)
                    .newInstance(val, filteredSuggestions);

            BukkitPacketAnalyzer.sendPacket(player.getUniqueId(), packet);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return false;
    }

    private String getSuggestionFromEntry(Object suggestionObj) {
        try {
            List<Field> fields = Reflection.getFieldsByTypeNormal(suggestionObj.getClass(), "String", Reflection.SearchOption.ENDS);
            if(fields.isEmpty()) return "";

            Field field = fields.get(0);
            String result = (String) field.get(suggestionObj);
            field.setAccessible(false);
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return "";
    }
}