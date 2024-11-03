package de.rayzs.pat.utils.subargs;

import de.rayzs.pat.plugin.subargs.SubArgs;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
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

    public static List<String> getResponse(String input) {
        return getResponse(input, Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE.getLines());
    }

    public static List<String> getResponse(String input, List<String> defaultMessage) {
        final String finalInput = input.startsWith("/") ? StringUtils.replaceFirst(input, "/", "") : input;

        final Optional<Response> optionalResponse = RESPONSES.stream().filter(response -> {
            for (String trigger : response.triggers)
                if(trigger.endsWith("*") ? finalInput.startsWith(StringUtils.replace(trigger, "*", "")) : finalInput.equals(trigger))
                    return true;

            return false;
        }).findFirst();
        return optionalResponse.isPresent() ? optionalResponse.get().message : defaultMessage;
    }

    private static class Response {

        private final List<String> triggers, message;

        public Response(final String key) {
            this.triggers = (List<String>) Storage.Files.CUSTOM_RESPONSES.get(key + ".triggers");
            this.message = (List<String>) Storage.Files.CUSTOM_RESPONSES.getOrSet(key + ".message", SubArgs.BLOCKED_MESSAGE);
        }

    }
}
