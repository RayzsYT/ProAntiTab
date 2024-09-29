package de.rayzs.pat.api.event;

import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.CommunicationPackets;

import java.util.*;

public class PATEventHandler {

    private static List<PATEvent> EVENTS = new ArrayList<>();

    public static ExecuteCommandEvent call(Object senderObj, String command, boolean blocked) {
        ExecuteCommandEvent event = EmptyEvent.createEmptyExecuteCommandEvent(senderObj, command, blocked);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof ExecuteCommandEvent) {
                ExecuteCommandEvent executeCommandEvent = (ExecuteCommandEvent) patEvent;
                executeCommandEvent.handle(event);
            }
        }

        return event;
    }

    public static FilteredSuggestionEvent call(Object senderObj, List<String> suggestions) {
        FilteredSuggestionEvent event = EmptyEvent.createEmptyFilteredSuggestionEvent(senderObj, suggestions);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof FilteredSuggestionEvent) {
                FilteredSuggestionEvent filteredSuggestion = (FilteredSuggestionEvent) patEvent;
                filteredSuggestion.handle(event);
            }
        }

        return event;
    }

    public static FilteredTabCompletionEvent call(Object senderObj, String cursor, List<String> completion) {
        FilteredTabCompletionEvent event = EmptyEvent.createEmptyFilteredTabCompletion(senderObj, cursor, completion);

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

    public static UpdatePlayerCommandsEvent call(Object playerObj, List<String> commands, boolean serverBased) {
        UpdatePlayerCommandsEvent event = EmptyEvent.createEmptyUpdatePlayerCommandsEvent(playerObj, commands, serverBased);

        for (PATEvent patEvent : EVENTS) {
            if(patEvent instanceof UpdatePlayerCommandsEvent) {
                UpdatePlayerCommandsEvent updatePlayerCommandsEvent = (UpdatePlayerCommandsEvent) patEvent;
                updatePlayerCommandsEvent.handle(event);
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

        public static UpdatePlayerCommandsEvent createEmptyUpdatePlayerCommandsEvent(Object playerObj, List<String> commands, boolean serverBased) {
            return new UpdatePlayerCommandsEvent(playerObj, commands, serverBased) {
                @Override
                public void handle(UpdatePlayerCommandsEvent event) {

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

        public static ExecuteCommandEvent createEmptyExecuteCommandEvent(Object senderObj, String command, boolean blocked) {
            return new ExecuteCommandEvent(senderObj, command, blocked) {
                @Override
                public void handle(ExecuteCommandEvent event) {

                }
            };
        }

        public static FilteredSuggestionEvent createEmptyFilteredSuggestionEvent(Object senderObj, List<String> suggestions) {
            return new FilteredSuggestionEvent(senderObj, suggestions) {
                @Override
                public void handle(FilteredSuggestionEvent event) {

                }
            };
        }

        public static FilteredTabCompletionEvent createEmptyFilteredTabCompletion(Object senderObj, String cursor, List<String> completion) {
            return new FilteredTabCompletionEvent(senderObj, cursor, completion) {
                @Override
                public void handle(FilteredTabCompletionEvent event) {

                }
            };
        }

    }
}
