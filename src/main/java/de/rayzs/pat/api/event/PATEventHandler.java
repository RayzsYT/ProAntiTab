package de.rayzs.pat.api.event;

import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.CommunicationPackets;

import java.util.*;

public class PATEventHandler {

    private static List<PATEvent> EVENTS = new ArrayList<>();

    public static ExecuteCommandEvent call(UUID senderUniqueId, String command, boolean blocked) {
        ExecuteCommandEvent event = EmptyEvent.createEmptyExecuteCommandEvent(senderUniqueId, command, blocked);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof ExecuteCommandEvent) {
                ExecuteCommandEvent executeCommandEvent = (ExecuteCommandEvent) patEvent;
                executeCommandEvent.handle(event);
            }
        }

        return event;
    }

    public static FilteredSuggestionEvent call(UUID senderUniqueId, List<String> suggestions) {
        FilteredSuggestionEvent event = EmptyEvent.createEmptyFilteredSuggestionEvent(senderUniqueId, suggestions);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof FilteredSuggestionEvent) {
                FilteredSuggestionEvent filteredSuggestion = (FilteredSuggestionEvent) patEvent;
                filteredSuggestion.handle(event);
            }
        }

        return event;
    }

    public static FilteredTabCompletionEvent call(UUID senderUniqueId, String cursor, List<String> completion) {
        FilteredTabCompletionEvent event = EmptyEvent.createEmptyFilteredTabCompletion(senderUniqueId, cursor, completion);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof FilteredTabCompletionEvent) {
                FilteredTabCompletionEvent filteredTabCompletion = (FilteredTabCompletionEvent) patEvent;
                filteredTabCompletion.handle(event);
            }
        }

        return event;
    }

    public static UpdatePluginEvent call() {
        UpdatePluginEvent event = EmptyEvent.createEmptyUpdatePluginEvent();

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof UpdatePluginEvent) {
                UpdatePluginEvent updatePluginEvent = (UpdatePluginEvent) patEvent;
                updatePluginEvent.handle(event);
            }
        }

        return event;
    }

    public static SentSyncEvent call(CommunicationPackets.PacketBundle packetBundle, String serverName) {
        SentSyncEvent event = EmptyEvent.createEmptySentSyncEvent(packetBundle, serverName);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof SentSyncEvent) {
                SentSyncEvent sentSyncEvent = (SentSyncEvent) patEvent;
                sentSyncEvent.handle(event);
            }
        }

        return event;
    }

    public static ReceiveSyncEvent call(CommunicationPackets.PacketBundle packetBundle) {
        ReceiveSyncEvent event = EmptyEvent.createEmptyReceiveSyncEvent(packetBundle);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof ReceiveSyncEvent) {
                ReceiveSyncEvent proxySyncEvent = (ReceiveSyncEvent) patEvent;
                proxySyncEvent.handle(event);
            }
        }

        return event;
    }

    public static void register(PATEvent event) {
        EVENTS.add(event);
    }

    public static void unregister(PATEvent event) {
        EVENTS.remove(event);
    }

    public static class EmptyEvent {

        public static SentSyncEvent createEmptySentSyncEvent(CommunicationPackets.PacketBundle packetBundle, String serverName) {
            return new SentSyncEvent(null, packetBundle, serverName) {
                @Override
                public void handle(SentSyncEvent event) {

                }
            };
        }

        public static ReceiveSyncEvent createEmptyReceiveSyncEvent(CommunicationPackets.PacketBundle packetBundle) {
            return new ReceiveSyncEvent(null, packetBundle) {
                @Override
                public void handle(ReceiveSyncEvent event) {

                }
            };
        }

        public static UpdatePluginEvent createEmptyUpdatePluginEvent() {
            return new UpdatePluginEvent(null) {
                @Override
                public void handle(UpdatePluginEvent event) {

                }
            };
        }

        public static ExecuteCommandEvent createEmptyExecuteCommandEvent(UUID senderUniqueId, String command, boolean blocked) {
            return new ExecuteCommandEvent(senderUniqueId, command, blocked) {
                @Override
                public void handle(ExecuteCommandEvent event) {

                }
            };
        }

        public static FilteredSuggestionEvent createEmptyFilteredSuggestionEvent(UUID senderUniqueId, List<String> suggestions) {
            return new FilteredSuggestionEvent(senderUniqueId, suggestions) {
                @Override
                public void handle(FilteredSuggestionEvent event) {

                }
            };
        }

        public static FilteredTabCompletionEvent createEmptyFilteredTabCompletion(UUID senderUniqueId, String cursor, List<String> completion) {
            return new FilteredTabCompletionEvent(senderUniqueId, cursor, completion) {
                @Override
                public void handle(FilteredTabCompletionEvent event) {

                }
            };
        }

    }
}
