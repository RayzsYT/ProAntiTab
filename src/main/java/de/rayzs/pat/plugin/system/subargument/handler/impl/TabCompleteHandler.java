package de.rayzs.pat.plugin.system.subargument.handler.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.system.subargument.handler.SubArgumentHandler;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.NumberUtils;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.plugin.system.subargument.argument.Arguments;

import java.util.ArrayList;
import java.util.List;

public class TabCompleteHandler extends SubArgumentHandler {

    public TabCompleteHandler(SubArgument instance) {
        super(instance);
    }

    public List<String> handleTabCompletion(CommandSender sender, String cursor, List<String> suggestions) {
        cursor = cursor.substring(1);

        if (suggestions.isEmpty()) {
            return suggestions;
        }


        // Removes all sub-arguments if it's any of these commands and if they are enabled...
        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(cursor) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(cursor)) {
            return List.of();
        }

        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;
        final Arguments arguments = getInstance().getPlayerArgument(sender);

        final List<String> negated = new ArrayList<>(arguments.getResultTab("!" + cursor));
        final List<String> negatedInputs = new ArrayList<>(arguments.TAB_ARGUMENTS.INPUTS).stream().filter(s -> {
            if (s.isEmpty())
                return false;

            boolean n = s.charAt(0) == '!';

            if (!n && s.length() > 5)
                n = s.charAt(5) == '!';

            return n;
        }).map(s -> s.substring(1 + (s.charAt(0) == '[' ? 5 : 0))).toList();



        if (negatedInputs.stream().anyMatch(cursor::startsWith)) {
            if (turn) {
                return List.of();
            }

            return suggestions;
        }

        List<String> possibilities = new ArrayList<>(suggestions), result = new ArrayList<>(arguments.getResultTab(cursor));

        result.removeIf(s -> s.contains("-_"));

        if (turn) {
            possibilities = result;

            if (possibilities.isEmpty() && !negated.isEmpty()) {
                possibilities = suggestions;
                possibilities.removeAll(negated);
            }

            if (result.contains("%numbers%")) {
                possibilities.remove("%numbers%");
                possibilities.addAll(suggestions.stream().filter(NumberUtils::isDigit).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");
            }

            if (result.contains("%players%")) {
                possibilities.remove("%players%");
                possibilities.addAll(getInstance().getCachedPlayerNames());
            }

            if (result.contains("%online_players%")) {
                possibilities.remove("%online_players%");
                possibilities.addAll(getInstance().getCachedOnlinePlayerNames());
            }

            if (result.contains("%offline_players%")) {
                possibilities.remove("%offline_players%");
                possibilities.removeAll(getInstance().getCachedOnlinePlayerNames());
            }

            if (result.contains("%hidden_players%")) {
                possibilities.remove("%hidden_players%");

                possibilities.addAll(suggestions.stream().filter(completion -> {
                    if (getInstance().getCachedPlayerNames().contains(completion))
                        return false;

                    return result.contains(completion);
                }).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");

            }

            if (result.contains("%hidden_online_players%")) {
                possibilities.remove("%hidden_online_players%");

                possibilities.addAll(suggestions.stream().filter(completion -> {
                    if (getInstance().getCachedOnlinePlayerNames().contains(completion))
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
                possibilities.removeIf(possibility -> getInstance().getCachedPlayerNames().contains(possibility));

            if (result.contains("%offline_players%"))
                possibilities.removeIf(possibility -> !getInstance().getCachedPlayerNames().contains(possibility));

            if (result.contains("%online_players%"))
                possibilities.removeIf(possibility -> getInstance().getCachedOnlinePlayerNames().contains(possibility));


            possibilities.removeIf(s -> {
                if (negated.contains(s)) {
                    return false;
                }

                return result.contains(s);
            });
        }

        if (possibilities.isEmpty()) {
            return suggestions;
        }

        return possibilities;
    }
}
