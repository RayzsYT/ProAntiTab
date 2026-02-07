package de.rayzs.pat.utils.response;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.plugin.subarguments.SubArguments;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.response.action.ActionHandler;

import java.util.*;
import java.util.stream.Collectors;

public class ResponseHandler {

    private static List<Response> RESPONSES = new ArrayList<>();

    public static void update() {
        RESPONSES = new ArrayList<>();

        Storage.Files.CUSTOM_RESPONSES.getKeys(true).stream().filter(key -> !key.equals("default-message")).forEach(key -> {
            if (!key.contains(".")) {
                Response response = new Response(key);
                RESPONSES.add(response);
            }
        });
    }

    public static String replaceArgsVariables(String input, String command) {
        try {
            final char[] chars = input.toCharArray();
            final String placeholder = "%args[";

            int index = -1;
            int found = 0;

            for (int i = 0; i < chars.length; i++) {
                final char c = chars[i];

                if (c != placeholder.charAt(found)) {
                    if (found > 0) {
                        i--;
                    }

                    found = 0;
                    continue;
                }

                found++;

                if (found == placeholder.length()) {
                    index = i - placeholder.length() + 1;
                    break;
                }
            }

            if (index == -1) {
                return input;
            }

            String argument = input.substring(index);
            final int endIndex = argument.substring(2).indexOf(']');

            if (endIndex != -1) {
                argument = argument.substring(0, endIndex + 4);

                String[] commandSplit = command.split(" ");

                final String argumentInsides = argument.substring(6, argument.length() - 2);
                int leftNum = -1, rightNum = -1;

                if (argumentInsides.contains("-")) {
                    final String[] numSplit = argumentInsides.split("-");

                    leftNum = Integer.parseInt(numSplit[0]);
                    rightNum = Integer.parseInt(numSplit[1]);
                } else if (argumentInsides.startsWith("_")) {
                    leftNum = 0;
                    rightNum = Integer.parseInt(argumentInsides.replace("_", ""));
                } else if (argumentInsides.endsWith("_")) {
                    leftNum = Integer.parseInt(argumentInsides.replace("_", ""));
                    rightNum = commandSplit.length;
                }

                final StringBuilder replacementCommand = new StringBuilder();

                if (leftNum == -1 && rightNum == -1) {
                    final int i = Integer.parseInt(argumentInsides) - 1;
                    replacementCommand.append(commandSplit[Math.min(commandSplit.length - 1, i)]);
                } else {
                    final int min = Math.max(0, leftNum + -1);
                    final int max = Math.min(rightNum, commandSplit.length);

                    for (int i = min; i < max; i++) {
                        if (i != min) replacementCommand.append(" ");
                        replacementCommand.append(commandSplit[i]);
                    }
                }

                input = input.substring(0, index) + replacementCommand + input.substring(index + endIndex + 4);
            }
        } catch (Exception exception) {
            Logger.warning("Failed to parse and replace %args[...]% parts. Please make sure that the format is correct and that befitting the written numbers.");
            Logger.warning("Check examples from the custom-responses.yml file for comparison.");
            return input;
        }

        return replaceArgsVariables(input, command);
    }

    public static List<String> getResponse(UUID uuid, String playerName, String serverName, String input) {
        return getResponse(uuid, playerName, serverName, input, Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE.getLines());
    }

    public static List<String> getResponse(UUID uuid, String playerName, String serverName, String input, List<String> defaultMessage) {
        final String finalInput = input.startsWith("/") && input.length() > 1
                ? input.substring(1)
                : input;

        final Optional<Response> optionalResponse = RESPONSES.stream().filter(response -> {
            for (String trigger : response.triggers)
                if (trigger.endsWith("*") ? finalInput.startsWith(StringUtils.replace(trigger, "*", "")) : finalInput.equals(trigger))
                    return true;

            return false;
        }).findFirst();

        if (uuid != null && optionalResponse.isPresent()) {
            optionalResponse.get().executeAction(uuid, playerName, serverName, finalInput);

            return new ArrayList<>(optionalResponse.get().message)
                    .stream().map(s -> replaceArgsVariables(s, finalInput))
                    .collect(Collectors.toList());
        }

        return defaultMessage;
    }

    private static class Response {

        private final List<String> triggers, message, actions;

        public Response(final String key) {
            this.triggers = (List<String>) Storage.Files.CUSTOM_RESPONSES.get(key + ".triggers");
            this.message = (List<String>) Storage.Files.CUSTOM_RESPONSES.getOrSet(key + ".message", SubArguments.BLOCKED_MESSAGE);
            this.actions = (List<String>) Storage.Files.CUSTOM_RESPONSES.getOrSet(key + ".actions", new ArrayList<>());
        }

        public void executeAction(UUID uuid, String playerName, String serverName, String command) {
            String[] split;

            for (String action : actions) {

                if (action.equals("alert")) {
                    ActionHandler.alert(uuid, playerName, serverName, command);
                    continue;
                }

                if (!action.contains("::")) {
                    Logger.warning("! Could not recognise action: " + action);
                    Logger.warning("  > Syntax does not match at all. Have you used the splitters (::) correctly?");
                    continue;
                }

                split = action.split("::");

                switch (split[0].toLowerCase()) {
                    case "effect":
                        if (Reflection.isProxyServer()) {
                            Logger.warning("! Could not execute action: " + action);
                            Logger.warning("  > Effect cannot be added on the proxy side!");
                            continue;
                        }

                        if (split.length != 4) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 4 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > effect::potionEffect::duration::amplifier");
                            Logger.warning("  > e.g: effect::BLINDNESS::100::1");
                            continue;
                        }

                        int duration, amplifier;

                        try {
                            duration = Integer.parseInt(split[2]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Duration " + split[2].toUpperCase() + " is not a valid integer! (e.g: 1)");
                            continue;
                        }

                        try {
                            amplifier = Integer.parseInt(split[3]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Amplifier " + split[3].toUpperCase() + " is not a valid integer! (e.g: 1)");
                            continue;
                        }

                        ActionHandler.addPotionEffect(action, uuid, split[1].toUpperCase(), duration, amplifier);
                        continue;

                    case "title":
                        if (split.length != 6) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 6 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > title::title::subtitle::fadeIn::stay::fadeOut");
                            Logger.warning("  > e.g: title::My title::My subtitle::300::3000::300");
                            continue;
                        }

                        int fadeIn, stay, fadeOut;

                        try {
                            fadeIn = Integer.parseInt(split[3]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Fade-In " + split[3].toUpperCase() + " is not a valid integer! (e.g: 300)");
                            continue;
                        }

                        try {
                            stay = Integer.parseInt(split[4]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Stay " + split[4].toUpperCase() + " is not a valid integer! (e.g: 3000)");
                            continue;
                        }

                        try {
                            fadeOut = Integer.parseInt(split[5]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Fade-Out " + split[5].toUpperCase() + " is not a valid integer! (e.g: 300)");
                            continue;
                        }

                        ActionHandler.sendTitle(action, uuid, command, split[1], split[2], fadeIn, stay, fadeOut);
                        continue;

                    case "sound":
                        if (split.length != 4) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 4 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > sound::soundName::volume::pitch");
                            Logger.warning("  > e.g: sound::" + (Reflection.isProxyServer() ? "entity.ender_dragon.growl:" : "ENTITY_ENDER_DRAGON_GROWL") + "::1.0::1.0");
                            continue;
                        }

                        float volume, pitch;

                        try {
                            volume = Float.parseFloat(split[2]);

                            if (volume < 0 || volume > 10) {
                                Logger.warning("! Failed to read action: " + action);
                                Logger.warning("  > Your volume (" + split[2].toUpperCase() + ") must be between 0.0 and 10.0! (e.g: 1.0)");
                                continue;
                            }

                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Volume " + split[2].toUpperCase() + " is not a valid float! (e.g: 1.0f)");
                            continue;
                        }

                        try {
                            pitch = Float.parseFloat(split[3]);

                            if (pitch < 0 || pitch > 10) {
                                Logger.warning("! Failed to read action: " + action);
                                Logger.warning("  > Your pitch (" + split[2].toUpperCase() + ") must be between 0.0 and 2.0! (e.g: 1.0)");
                                continue;
                            }
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Pitch " + split[3].toUpperCase() + " is not a valid float! (e.g: 1.0)");
                            continue;
                        }

                        ActionHandler.playSound(
                                action,
                                uuid,
                                Reflection.isProxyServer() ? split[1].toLowerCase() : split[1].toUpperCase(),
                                volume,
                                pitch
                        );

                        continue;

                    case "console":
                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > console::command");
                            Logger.warning("  > e.g: console::say Hello world!");
                            continue;
                        }

                        ActionHandler.executeConsoleCommand(action, uuid, command, split[1]);
                        continue;

                    case "execute":
                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > execute::command");
                            Logger.warning("  > e.g: execute::help");
                            continue;
                        }

                        ActionHandler.executePlayerCommand(action, uuid, command, split[1]);
                        continue;

                    case "actionbar":
                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > actionbar::text");
                            Logger.warning("  > e.g: actionbar::I like COOKIES");
                            continue;
                        }

                        ActionHandler.sendActionbar(action, uuid, command, split[1]);
                        continue;

                    case "p2b-message":

                        if (!Reflection.isProxyServer()) {
                            Logger.warning("! Could not execute action: " + action);
                            Logger.warning("  > Cannot send p2b-packets on a backend server!");
                            Logger.warning("  > This only works on proxy servers!");
                            Logger.warning("  > P2B -> Proxy to Backend");
                            continue;
                        }

                        if (serverName == null) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Failed to fetch servername player is on!");
                            continue;
                        }

                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > p2b-message::text");
                            Logger.warning("  > e.g: p2b-message::&cUser %player% is evil!");
                            continue;
                        }

                        Communicator.sendP2BMessage(
                                serverName,
                                ResponseHandler.replaceArgsVariables(
                                        StringUtils.replace(split[1], "%player%", playerName),
                                        command
                                )
                        );
                        continue;

                    case "p2b-execute":

                        if (!Reflection.isProxyServer()) {
                            Logger.warning("! Could not execute action: " + action);
                            Logger.warning("  > Cannot send p2b-packets on a backend server!");
                            Logger.warning("  > This only works on proxy servers!");
                            Logger.warning("  > P2B -> Proxy to Backend");
                            continue;
                        }

                        if (serverName == null) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Failed to fetch servername player is on!");
                            continue;
                        }

                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > p2b-execute::command");
                            Logger.warning("  > e.g: p2b-execute::help");
                            continue;
                        }

                        Communicator.sendP2BExecute(
                                serverName,
                                uuid,
                                ResponseHandler.replaceArgsVariables(
                                        StringUtils.replace(split[1], "%player%", playerName),
                                        command
                                )
                        );
                        continue;

                    case "p2b-console":

                        if (!Reflection.isProxyServer()) {
                            Logger.warning("! Could not execute action: " + action);
                            Logger.warning("  > Cannot send p2b-packets on a backend server!");
                            Logger.warning("  > This only works on proxy servers!");
                            Logger.warning("  > P2B -> Proxy to Backend");
                            continue;
                        }

                        if (serverName == null) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Failed to fetch servername player is on!");
                            continue;
                        }

                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > p2b-console::command");
                            Logger.warning("  > e.g: p2b-console::help");
                            continue;
                        }

                        Communicator.sendP2BExecute(
                                serverName,
                                null,
                                ResponseHandler.replaceArgsVariables(
                                        StringUtils.replace(split[1], "%player%", playerName),
                                        command
                                )
                        );
                }

            }
        }
    }
}
