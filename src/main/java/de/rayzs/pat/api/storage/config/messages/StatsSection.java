package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;

public class StatsSection extends ConfigStorage {

    public String NO_SERVER, SPLITTER, SERVER;
    public MultipleMessagesHelper STATISTIC;

    public StatsSection() {
        super("stats");
    }

    @Override
    public void load() {
        super.load();
        NO_SERVER = new ConfigSectionHelper<String>(this, "no-server", "&cNone!").getOrSet();
        SPLITTER = new ConfigSectionHelper<String>(this, "message.splitter", "&7, ").getOrSet();
        SERVER = new ConfigSectionHelper<String>(this, "message.server", "&f%servername% &8(%updated%)").getOrSet();
        STATISTIC = new MultipleMessagesHelper(this, "message.statistic", Arrays.asList(
                "&7Last sync sent to &f%server_count% &7servers. &8&o(%last_sync_time% ago)",
                "&7Sent to servers: &f%servers%"
                ));
    }
}
