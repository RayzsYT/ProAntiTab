package de.rayzs.pat.api.event;

import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;

public class PATEventHandler {

    private static List<PATEvent> EVENTS = new ArrayList<>();

    public static ExecuteCommandEvent callExecuteCommandEvents(CommandSender player, String command, boolean blocked, boolean notify) {
        ExecuteCommandEvent event = EmptyEvent.createEmptyExecuteCommandEvent(player, command, blocked, notify);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof ExecuteCommandEvent executeCommandEvent) {
                executeCommandEvent.handle(event);
            }
        }

        return event;
    }

    public static FilteredSuggestionEvent callFilteredSuggestionEvents(CommandSender player, List<String> suggestions) {
        FilteredSuggestionEvent event = EmptyEvent.createEmptyFilteredSuggestionEvent(player, suggestions);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof FilteredSuggestionEvent filteredSuggestionEvent) {
                filteredSuggestionEvent.handle(event);
            }
        }

        return event;
    }

    public static FilteredTabCompletionEvent callFilteredTabCompletionEvents(CommandSender player, String cursor, List<String> completion) {
        FilteredTabCompletionEvent event = EmptyEvent.createEmptyFilteredTabCompletion(player, cursor, completion);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof FilteredTabCompletionEvent filteredTabCompletionEvent) {
                filteredTabCompletionEvent.handle(event);
            }
        }

        return event;
    }

    public static ServerPlayersChangeEvent callServerPlayersChangeEvents(CommandSender player, ServerPlayersChangeEvent.Type type) {
        ServerPlayersChangeEvent event = EmptyEvent.createEmptyServerPlayersChangeEvent(player, type);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof ServerPlayersChangeEvent serverPlayersChangeEvent) {
                serverPlayersChangeEvent.handle(event);
            }
        }

        return event;
    }

    public static UpdatePluginEvent callUpdatePluginEvents() {
        UpdatePluginEvent event = EmptyEvent.createEmptyUpdatePluginEvent();

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof UpdatePluginEvent updatePluginEvent) {
                updatePluginEvent.handle(event);
            }
        }

        return event;
    }

    public static UpdatePlayerCommandsEvent callUpdatePlayerCommandsEvents(CommandSender player, List<String> commands, boolean serverBased) {
        UpdatePlayerCommandsEvent event = EmptyEvent.createEmptyUpdatePlayerCommandsEvent(player, commands, serverBased);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof UpdatePlayerCommandsEvent updatePlayerCommandsEvent) {
                updatePlayerCommandsEvent.handle(event);
            }
        }

        return event;
    }

    public static SentSyncEvent callSentSyncEvents(CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket, String serverName) {
        SentSyncEvent event = EmptyEvent.createEmptySentSyncEvent(dataSyncPacket, serverName);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof SentSyncEvent sentSyncEvent) {
                sentSyncEvent.handle(event);
            }
        }

        return event;
    }

    public static ReceiveSyncEvent callReceiveSyncEvents(CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket) {
        ReceiveSyncEvent event = EmptyEvent.createEmptyReceiveSyncEvent(dataSyncPacket);

        for (PATEvent patEvent : EVENTS) {
            if (patEvent instanceof ReceiveSyncEvent receiveSyncEvent) {
                receiveSyncEvent.handle(event);
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

        public static ServerPlayersChangeEvent createEmptyServerPlayersChangeEvent(CommandSender player, ServerPlayersChangeEvent.Type type) {
            return new ServerPlayersChangeEvent(player, type) {
                @Override
                public void handle(ServerPlayersChangeEvent event) {

                }
            };
        }

        public static SentSyncEvent createEmptySentSyncEvent(CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket, String serverName) {
            return new SentSyncEvent(null, dataSyncPacket, serverName) {
                @Override
                public void handle(SentSyncEvent event) {

                }
            };
        }

        public static ReceiveSyncEvent createEmptyReceiveSyncEvent(CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket) {
            return new ReceiveSyncEvent(null, dataSyncPacket) {
                @Override
                public void handle(ReceiveSyncEvent event) {

                }
            };
        }

        public static UpdatePlayerCommandsEvent createEmptyUpdatePlayerCommandsEvent(CommandSender player, List<String> commands, boolean serverBased) {
            return new UpdatePlayerCommandsEvent(player, commands, serverBased) {
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

        public static ExecuteCommandEvent createEmptyExecuteCommandEvent(CommandSender player, String command, boolean blocked, boolean notify) {
            return new ExecuteCommandEvent(player, command, blocked, notify) {
                @Override
                public void handle(ExecuteCommandEvent event) {

                }
            };
        }

        public static FilteredSuggestionEvent createEmptyFilteredSuggestionEvent(CommandSender player, List<String> suggestions) {
            return new FilteredSuggestionEvent(player, suggestions) {
                @Override
                public void handle(FilteredSuggestionEvent event) {

                }
            };
        }

        public static FilteredTabCompletionEvent createEmptyFilteredTabCompletion(CommandSender player, String cursor, List<String> completion) {
            return new FilteredTabCompletionEvent(player, cursor, completion) {
                @Override
                public void handle(FilteredTabCompletionEvent event) {

                }
            };
        }

    }
}
