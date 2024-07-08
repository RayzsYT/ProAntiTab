package de.rayzs.pat.api.event;

import java.util.*;

public class PATEventManager {

    private static final List<PATEvent> EVENTS = new LinkedList<>();

    public static void register(PATEvent event) {
        if(!EVENTS.contains(event))
            EVENTS.add(event);
    }

    public static void unregister(PATEvent event) {
        EVENTS.remove(event);
    }

    public static void unregisterAll(PATEvent event) {
        EVENTS.clear();
    }

    public static boolean useDefaultActions(Object executor, String command, PATEvent.Situation situation) {
        for (PATEvent event : EVENTS) {
            if(event.situation != situation) continue;
            if(!event.shouldHandleByDefault(executor, command)) return false;
        }

        return true;
    }
}
