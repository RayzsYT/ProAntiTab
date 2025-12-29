package de.rayzs.pat.plugin.listeners.bukkit;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import org.bukkit.event.*;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.plaf.BorderUIResource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PaperServerListPing implements Listener {

    private static final ExpireCache<Integer, List<ProtocolHoverLine>> CACHED_PROTOCOL_NAMES = new ExpireCache<>(1, TimeUnit.SECONDS);
    private static final UUID RANDOM_UUID = UUID.randomUUID();
    private static final int CACHE_KEY = 0;

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPaperServerListPing(PaperServerListPingEvent event) {
        if(!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;
        if(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW) event.setProtocolVersion(0);

        int online = Bukkit.getOnlinePlayers().size(),
                onlineExtend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT,
                max = Bukkit.getMaxPlayers();

        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_EXTEND_AS_MAX_COUNT)
            event.setMaxPlayers(onlineExtend);

        String versionName = Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL.replace("%online_extended%", String.valueOf(onlineExtend)).replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max));
        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.HIDE_PLAYERS) {
            event.getPlayerSample().clear();
        }

        else if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_CUSTOM_PLAYERLIST) {
            List<ProtocolHoverLine> cachedPlayerNames = CACHED_PROTOCOL_NAMES.get(CACHE_KEY);

            event.getPlayerSample().clear();

            if (cachedPlayerNames != null) {

                event.getPlayerSample().addAll(cachedPlayerNames);

            } else {

                Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PLAYERLIST.getLines().forEach(line -> {

                    if (line.contains("%players%")) {
                        List<String> playerNames = Storage.getLoader().getOnlinePlayerNames();

                        playerNames = playerNames.size() > 30
                                ? playerNames.subList(0, 30)
                                : playerNames;

                        for (String currentPlayerName : playerNames) {
                            String playerName = StringUtils.replace(line,
                                    "%players%", currentPlayerName, "&", "§"
                            );

                            event.getPlayerSample().add(new ProtocolHoverLine(playerName, RANDOM_UUID));
                        }

                    } else {
                        String playerName = replaceString(line, online, onlineExtend, max);

                        if (playerName.contains("\n")) {
                            String[] nameSplit = playerName.split("\n");
                            nameSplit = nameSplit.length > 30
                                    ? Arrays.copyOfRange(nameSplit, 0, 30)
                                    : nameSplit;

                            for (String name : nameSplit) {
                                event.getPlayerSample().add(new ProtocolHoverLine(name, RANDOM_UUID));
                            }

                        } else {
                            event.getPlayerSample().add(new ProtocolHoverLine(playerName, RANDOM_UUID));
                        }
                    }
                });
            }
        }

        event.setVersion(replaceString(versionName, online, onlineExtend, max));
    }

    private String replaceString(String string, int online, int onlineExtend, int max) {
        String replaced = string
                .replace("&", "§")
                .replace("%online_extended%", String.valueOf(onlineExtend))
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max));

        return replaced.contains("%") ? PlaceholderReplacer.replace(null, replaced) : replaced;
    }

    private static class ProtocolHoverLine implements PlayerProfile {

        private String name, id;

        public ProtocolHoverLine(String name, UUID id) {
            this.name = name;
            this.id = id.toString();
        }

        @Override
        public @Nullable UUID getUniqueId() {
            return UUID.fromString(id);
        }

        @Override
        public @Nullable String getName() {
            return name;
        }

        @Override
        public @NotNull String setName(@Nullable String s) {
            return "";
        }

        @Override
        public @Nullable UUID getId() {
            return getUniqueId();
        }

        @Override
        public @Nullable UUID setId(@Nullable UUID uuid) {
            return null;
        }

        @Override
        public @NotNull PlayerTextures getTextures() {
            return null;
        }

        @Override
        public void setTextures(@Nullable PlayerTextures playerTextures) {

        }

        @Override
        public Set<ProfileProperty> getProperties() {
            return null;
        }

        @Override
        public boolean hasProperty(@Nullable String s) {
            return false;
        }

        @Override
        public void setProperty(@NotNull ProfileProperty profileProperty) {

        }

        @Override
        public void setProperties(@NotNull Collection<ProfileProperty> collection) {

        }

        @Override
        public boolean removeProperty(@Nullable String s) {
            return false;
        }

        @Override
        public void clearProperties() {

        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public boolean completeFromCache() {
            return false;
        }

        @Override
        public boolean completeFromCache(boolean b) {
            return false;
        }

        @Override
        public boolean completeFromCache(boolean b, boolean b1) {
            return false;
        }

        @Override
        public boolean complete(boolean b) {
            return false;
        }

        @Override
        public boolean complete(boolean b, boolean b1) {
            return false;
        }

        @Override
        public @NotNull CompletableFuture<PlayerProfile> update() {
            return null;
        }

        @Override
        public org.bukkit.profile.@NotNull PlayerProfile clone() {
            return null;
        }

        @Override
        public Map<String, Object> serialize() {
            return null;
        }
    }
}
