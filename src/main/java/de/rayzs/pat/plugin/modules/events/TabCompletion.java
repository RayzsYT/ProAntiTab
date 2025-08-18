package de.rayzs.pat.plugin.modules.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import de.rayzs.pat.utils.subargs.Arguments;

import java.util.*;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        String cursor = event.getCursor().substring(1);

        if (event.getCompletion().isEmpty()) return;

        UUID uuid = event.getSenderObj() instanceof UUID
                ? (UUID) event.getSenderObj()
                : CommandSenderHandler.from(event.getSenderObj()).getUniqueId();

        Arguments arguments = SubArgsModule.PLAYER_COMMANDS.getOrDefault(uuid, Arguments.ARGUMENTS);

        List<String> possibilities = event.getCompletion(), result = new ArrayList<>(arguments.getResultTab(cursor));
        List<String> negated = new ArrayList<>(arguments.getResultTab("!" + cursor));


        result.removeIf(s -> s.contains("-_"));

        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            possibilities = result;

            if (possibilities.isEmpty() && !negated.isEmpty()) {
                possibilities = event.getCompletion();
                possibilities.removeAll(negated);
            }

            if (result.contains("%numbers%")) {
                possibilities.remove("%numbers%");
                possibilities.addAll(event.getCompletion().stream().filter(NumberUtils::isDigit).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");
            }

            if (result.contains("%players%")) {
                possibilities.remove("%players%");
                possibilities.addAll(SubArgsModule.getPlayerNames());
            }

            if (result.contains("%online_players%")) {
                possibilities.remove("%online_players%");
                possibilities.addAll(SubArgsModule.getOnlinePlayerNames());
            }

            if (result.contains("%hidden_players%")) {
                possibilities.remove("%hidden_players%");

                possibilities.addAll(event.getCompletion().stream().filter(completion -> {
                    if (SubArgsModule.getPlayerNames().contains(completion))
                        return false;

                    return result.contains(completion);
                }).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");

            }

            if (result.contains("%hidden_online_players%")) {
                possibilities.remove("%hidden_online_players%");

                possibilities.addAll(event.getCompletion().stream().filter(completion -> {
                    if (SubArgsModule.getOnlinePlayerNames().contains(completion))
                        return false;

                    return result.contains(completion);
                }).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");
            }

        } else {
            if (result.contains("%numbers%"))
                possibilities.removeIf(NumberUtils::isDigit);

            if (result.contains("%players%"))
                possibilities.removeIf(possibility -> SubArgsModule.getPlayerNames().contains(possibility));

            if (result.contains("%online_players%"))
                possibilities.removeIf(possibility -> SubArgsModule.getOnlinePlayerNames().contains(possibility));


            possibilities.removeIf(s -> {
                if (negated.contains(s)) {
                    return false;
                }

                return result.contains(s);
            });
        }

        cursor = StringUtils.getFirstArg(cursor.toLowerCase());
        if (possibilities.isEmpty() && !SubArgsModule.GENERAL_LIST.contains(cursor)) {
            return;
        }

        event.setCompletion(possibilities);
    }
}
