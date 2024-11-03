package de.rayzs.pat.addon.utils;

import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.StringUtils;

import java.util.*;

public class Responses {

    private static List<Response> RESPONSES = new ArrayList<>();
    private static List<String> DEFAULT_MESSAGE = new ArrayList<>();

    public static void update() {
        RESPONSES = new ArrayList<>();

        ArrayList<String> MESSAGE = new ArrayList<>(List.of("&8[&4ProAntiTab&8] &cThis argument is not allowed!"));
        DEFAULT_MESSAGE = (List<String>) SubArgsAddon.getConfiguration().getOrSet("default-message", MESSAGE);

        SubArgsAddon.getConfiguration().getKeys(true).stream().filter(key -> !key.equals("default-message")).forEach(key -> {
            if(!key.contains(".")) {
                Response response = new Response(key);
                RESPONSES.add(response);
            }
        });
    }

    public static List<String> getResponse(String input) {
        final String finalInput = input.startsWith("/") ? StringUtils.replaceFirst(input, "/", "") : input;

        final Optional<Response> optionalResponse = RESPONSES.stream().filter(response -> {
            for (String trigger : response.triggers)
                if(trigger.endsWith("*") ? finalInput.startsWith(StringUtils.replace(trigger, "*", "")) : finalInput.equals(trigger))
                    return true;

            return false;
        }).findFirst();
        return optionalResponse.isPresent() ? optionalResponse.get().message : DEFAULT_MESSAGE;
    }

    private static class Response {

        private final List<String> triggers, message;

        public Response(final String key) {
            this.triggers = (List<String>) SubArgsAddon.getConfiguration().get(key + ".triggers");
            this.message = (List<String>) SubArgsAddon.getConfiguration().getOrSet(key + ".message", SubArgsAddon.BLOCKED_MESSAGE);
        }

    }
}
