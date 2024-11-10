package de.rayzs.pat.utils.subargs;

import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class Responses {

    private static List<Response> RESPONSES = new ArrayList<>();

    public static void update() {
        RESPONSES = new ArrayList<>();

        Storage.Files.CUSTOM_RESPONSES.getKeys(true).stream().filter(key -> !key.equals("default-message")).forEach(key -> {
            if(!key.contains(".")) {
                Response response = new Response(key);
                RESPONSES.add(response);
            }
        });
    }

    public static List<String> getResponse(UUID uuid, String input) {
        return getResponse(uuid, input, Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE.getLines());
    }

    public static List<String> getResponse(UUID uuid, String input, List<String> defaultMessage) {
        final String finalInput = input.startsWith("/") ? StringUtils.replaceFirst(input, "/", "") : input;

        final Optional<Response> optionalResponse = RESPONSES.stream().filter(response -> {
            for (String trigger : response.triggers)
                if(trigger.endsWith("*") ? finalInput.startsWith(StringUtils.replace(trigger, "*", "")) : finalInput.equals(trigger))
                    return true;

            return false;
        }).findFirst();

        if(uuid != null && optionalResponse.isPresent()) {
            optionalResponse.get().executeAction(uuid);
            return optionalResponse.get().message;
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

        public void executeAction(UUID uuid) {
            String[] split;
            for (String action : actions) {
                if(!action.contains("::")) {
                    Logger.warning("Could not recognise action: " + action);
                    Logger.warning("Syntax does not match at all. Have you used the splitters (::) correctly?");
                    continue;
                }

                split = action.split("::");

                switch (split[0].toLowerCase()) {
                    case "title":
                        if(split.length != 3) {
                            Logger.warning("Could not recognise action: " + action);
                            Logger.warning("> Syntax does not match at all. Here's an example to compare with:");
                            Logger.warning("> title::My title::My subtitle");
                            continue;
                        }

                        String title = StringUtils.replace(split[1], "&", "§"),
                                subTitle = StringUtils.replace(split[2], "&", "§");
                        int fadeIn = 300, stay = 3000, fadeOut = 300;

                        if(!Reflection.isProxyServer())
                            BukkitLoader.sendTitle(uuid, title, subTitle, fadeIn, stay, fadeOut);
                        else {
                            if(Reflection.isVelocityServer())
                                VelocityLoader.sendTitle(uuid, title, subTitle, fadeIn, stay, fadeOut);
                            /*else
                                BungeeLoader.*/

                        }
                        continue;

                    case "sound":

                        if(Reflection.isProxyServer()) {
                            Logger.warning("Could not execute action: " + action);
                            Logger.warning("> Sounds cannot be played on the proxy side!");
                            continue;
                        }

                        if(split.length != 4) {
                            Logger.warning("Could not recognise action: " + action);
                            Logger.warning("> Syntax does not match at all. Here's an example to compare with:");
                            Logger.warning("> sound::ENTITY_ENDER_DRAGON_GROWL::1.0::1.0");
                            continue;
                        }

                        float volume, pitch;

                        try {
                            volume = Float.parseFloat(split[2]);
                        } catch (Exception exception) {
                            Logger.warning("Could not recognise action: " + action);
                            Logger.warning("> Volume " + split[2].toUpperCase() + " is not a valid float! (e.g: 1.0f)");
                            continue;
                        }

                        try {
                            pitch = Float.parseFloat(split[3]);
                        } catch (Exception exception) {
                            Logger.warning("Could not recognise action: " + action);
                            Logger.warning("> Pitch " + split[3].toUpperCase() + " is not a valid float (e.g: 1.0f)!");
                            continue;
                        }

                        BukkitLoader.playSound(uuid, split[1].toUpperCase(), volume, pitch);
                        continue;

                    case "console":
                        if(split.length == 1) {
                            Logger.warning("Could not recognise action: " + action);
                            Logger.warning("> Syntax does not match at all. Here's an example to compare with:");
                            Logger.warning("> console::say Hello world!");
                        }

                        if(!Reflection.isProxyServer())
                            BukkitLoader.executeConsoleCommand(split[1]);
                        else {
                            if (Reflection.isVelocityServer())
                                VelocityLoader.executeConsoleCommand(split[1]);
                            /*else
                                BungeeLoader.*/
                        }
                }

            }
        }
    }
}
