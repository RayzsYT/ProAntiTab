package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;
import de.rayzs.pat.utils.Reflection;

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

        if (!Reflection.isProxyServer()) {
            return;
        }

        NO_SERVER = new ConfigSectionHelper<String>(this, "no-server", "&cNone!").getOrSet();
        SPLITTER = new ConfigSectionHelper<String>(this, "message.splitter", "&7, ").getOrSet();
        SERVER = new ConfigSectionHelper<String>(this, "message.server", "&f%servername% &8(&a%last_alive_response% &8/ &2%updated%&8)").getOrSet();
        STATISTIC = new MultipleMessagesHelper(this, "message.statistic", Arrays.asList(
                "&7Format: &eserver (last alive packet / last sync packet)",
                "&7Last sync sent to &f%server_count% &7server(s) &e%last_sync_time% &7ago:",
                "&7Sent to servers: &f%servers%"
                ));
    }
}
