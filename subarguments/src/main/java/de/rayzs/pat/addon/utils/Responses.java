package de.rayzs.pat.addon.utils;

import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.StringUtils;

import java.util.*;

public class Responses {

    private static List<Response> RESPONSES = new ArrayList<>();
    private static List<String> DEFAULT_MESSAGE = new ArrayList<>();

    public static void update() {
        RESPONSES = new ArrayList<>();

        ArrayList<String> MESSAGE = (ArrayList<String>) List.of("&8[&4ProAntiTab&8] &cThis argument is not allowed!");
        DEFAULT_MESSAGE = (List<String>) SubArgsAddon.getConfiguration().getOrSet("default-message", MESSAGE);

        SubArgsAddon.getConfiguration().getKeys(true).stream().filter(key -> !key.equals("default-message")).forEach(key -> {
            Response response = new Response(key);
            RESPONSES.add(response);
        });
    }

    public static List<String> getResponse(String input) {
        final Optional<Response> optionalResponse = RESPONSES.stream().filter(response -> response.trigger.endsWith(" *") ? input.startsWith(StringUtils.replace(response.trigger, " *", "")) : input.equals(response.trigger)).findFirst();
        return optionalResponse.isPresent() ? optionalResponse.get().message : DEFAULT_MESSAGE;
    }

    private static class Response {

        private final String trigger;
        private final List<String> message;

        public Response(final String key) {
            this.trigger = (String) SubArgsAddon.getConfiguration().get(key + ".trigger");
            this.message = (List<String>) SubArgsAddon.getConfiguration().getOrSet(key + ".response", SubArgsAddon.BLOCKED_MESSAGE);
        }

    }
}
