package de.rayzs.pat.plugin.modules.subargs.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.subargs.Argument;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import java.util.*;
import java.util.stream.Collectors;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
        if (event.getCompletion().isEmpty()) return;

        UUID uuid = event.getSenderObj() instanceof UUID ? (UUID) event.getSenderObj() : new CommandSender(event.getSenderObj()).getUniqueId();
        Argument argument = SubArgsModule.PLAYER_COMMANDS.getOrDefault(uuid, Argument.getGeneralArgument());
        List<String> possibilities = event.getCompletion(), result = argument.getResult(cursor);

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            possibilities = result;
            if(result.contains("%online_players%")) {
                possibilities.remove("%online_players%");
                possibilities.addAll(SubArgsModule.getPlayerNames());
            }

            if(result.contains("%rmc_online_players%")) {
                possibilities.remove("%rmc_online_players%");
                possibilities.addAll(event.getCompletion().stream().filter(completion -> {
                    if(SubArgsModule.getPlayerNames().contains(completion)) return false;
                    return result.contains(completion);
                }).collect(Collectors.toList()));

                if(possibilities.isEmpty()) possibilities.add("///////////");
            }

        } else {
            if(result.contains("%online_players%"))
                possibilities.removeIf(possibility -> SubArgsModule.getPlayerNames().contains(possibility));

            possibilities.removeAll(result);
        }

        cursor = StringUtils.getFirstArg(cursor.toLowerCase());
        if(possibilities.isEmpty() && !SubArgsModule.GENERAL_LIST.contains(cursor)) return;

        event.setCompletion(possibilities);
    }
}
