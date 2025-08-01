package de.rayzs.pat.utils.response;

import de.rayzs.pat.plugin.modules.SubArgsModule;
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
        if (!input.contains(" "))
            return input;

        String[] commandSplit = command.contains(" ") ? command.split(" ") : new String[]{command};
        String[] split = input.split(" ");

        for (int i = 0; i < split.length; i++) {
            String part = split[i];

            if (! (part.startsWith("%args[") && part.endsWith("]%")))
                continue;

            part = part.substring(part.indexOf('[') + 1, part.lastIndexOf(']'));

            int length = commandSplit.length, fromIndex, toIndex;

            if (part.contains("-")) {
                String[] indexSplit = part.split("-");

                fromIndex = Integer.parseInt(indexSplit[0]) - 1;
                toIndex = Integer.parseInt(indexSplit[1]);
            } else {
                fromIndex = Integer.parseInt(part);
                toIndex = fromIndex;
            }

            if (fromIndex < 0 || toIndex > length) {
                Logger.warning("Custom response contains an error! (" + input + ")");
                Logger.warning("Out of bounds! (Input: " + input + ", Possible: " + (length > 1 ? "1-" : "") + length + ")");
                continue;
            }

            String replacement;

            if (fromIndex == toIndex) {
                replacement = commandSplit[fromIndex - 1];
            } else {
                replacement = String.join(" ", Arrays.copyOfRange(commandSplit, fromIndex, toIndex));;
            }

            split[i] = replacement;
        }

        return String.join(" ", split);
    }

    public static List<String> getResponse(UUID uuid, String input) {
        return getResponse(uuid, input, Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE.getLines());
    }

    public static List<String> getResponse(UUID uuid, String input, List<String> defaultMessage) {
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
            optionalResponse.get().executeAction(uuid, finalInput);

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
            this.message = (List<String>) Storage.Files.CUSTOM_RESPONSES.getOrSet(key + ".message", SubArgsModule.BLOCKED_MESSAGE);
            this.actions = (List<String>) Storage.Files.CUSTOM_RESPONSES.getOrSet(key + ".actions", new ArrayList<>());
        }

        public void executeAction(UUID uuid, String command) {
            String[] split;

            for (String action : actions) {
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

                        if (Reflection.isProxyServer()) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Sounds cannot be played on the proxy side!");
                            continue;
                        }

                        if (split.length != 4) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 4 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > sound::soundName::volume::pitch");
                            Logger.warning("  > e.g: sound::ENTITY_ENDER_DRAGON_GROWL::1.0::1.0");
                            continue;
                        }

                        float volume, pitch;

                        try {
                            volume = Float.parseFloat(split[2]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Volume " + split[2].toUpperCase() + " is not a valid float! (e.g: 1.0f)");
                            continue;
                        }

                        try {
                            pitch = Float.parseFloat(split[3]);
                        } catch (Exception exception) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Pitch " + split[3].toUpperCase() + " is not a valid float! (e.g: 1.0f)");
                            continue;
                        }

                        ActionHandler.playSound(action, uuid, split[1].toUpperCase(), volume, pitch);
                        continue;

                    case "console":
                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > console::command");
                            Logger.warning("  > e.g: console::say Hello world!");
                        }

                        ActionHandler.executeConsoleCommand(action, uuid, command, split[1]);

                    case "execute":
                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > execute::command");
                            Logger.warning("  > e.g: execute::help");
                        }

                        ActionHandler.executePlayerCommand(action, uuid, command, split[1]);

                    case "actionbar":
                        if (split.length == 1) {
                            Logger.warning("! Failed to read action: " + action);
                            Logger.warning("  > Action requires 1 arguments but only has " + split.length);
                            Logger.warning("  > Here's an example to compare with:");
                            Logger.warning("  > actionbar::text");
                            Logger.warning("  > e.g: actionbar::I like COOKIES");
                        }

                        ActionHandler.sendActionbar(action, uuid, command, split[1]);
                }

            }
        }
    }
}
