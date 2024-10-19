package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class ServerListSection extends ConfigStorage {

    public String SERVER_DOES_NOT_EXIST, GROUP_DOES_NOT_EXIST;
    public String LIST_SERVER_MESSAGE, LIST_SERVER_SPLITTER, LIST_SERVER_COMMAND, LIST_GROUP_MESSAGE, LIST_GROUP_SPLITTER, LIST_GROUP_COMMAND;

    public ServerListSection() {
        super("server-list");
    }

    @Override
    public void load() {
        super.load();
        if (!Reflection.isProxyServer()) return;

        SERVER_DOES_NOT_EXIST = new ConfigSectionHelper<String>(this, "server-not-found", "&cThe server %server% does not have any commands!").getOrSet();
        GROUP_DOES_NOT_EXIST = new ConfigSectionHelper<String>(this, "group-does-not-exist", "&cGroup %group% does not exist for %server%!").getOrSet();

        LIST_SERVER_MESSAGE = new ConfigSectionHelper<String>(this, "list.server.message", "&7Listed commands of %server% (&f%size%&7)&8: &f%commands%").getOrSet();
        LIST_SERVER_SPLITTER = new ConfigSectionHelper<String>(this, "list.server.splitter", "&7, ").getOrSet();
        LIST_SERVER_COMMAND = new ConfigSectionHelper<String>(this, "list.server.command", "&f%command%").getOrSet();

        LIST_GROUP_MESSAGE = new ConfigSectionHelper<String>(this, "list.group.message", "&7Listed commands of %group% from %server% (&f%size%&7)&8: &f%commands%").getOrSet();
        LIST_GROUP_SPLITTER = new ConfigSectionHelper<String>(this, "list.group.splitter", "&7, ").getOrSet();
        LIST_GROUP_COMMAND = new ConfigSectionHelper<String>(this, "list.group.command", "&f%command%").getOrSet();
    }
}
