package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class BlacklistSection extends ConfigStorage {

    public String CLEAR, CLEAR_CONFIRM, CLEAR_SERVER, CLEAR_SERVER_CONFIRM;
    public String LIST_MESSAGE, LIST_SPLITTER, LIST_COMMAND, LIST_SERVER_MESSAGE, LIST_SERVER_SPLITTER, LIST_SERVER_COMMAND;

    public String ADD_SUCCESS, ADD_FAILED;
    public String REMOVE_SUCCESS, REMOVE_FAILED;

    public String ADD_SERVER_SUCCESS, ADD_SERVER_FAILED;
    public String REMOVE_SERVER_SUCCESS, REMOVE_SERVER_FAILED;

    public BlacklistSection() {
        super("blacklist");
    }

    @Override
    public void load() {
        super.load();
        CLEAR = new ConfigSectionHelper<String>(this, "clear", "&aList has been cleared!").getOrSet();
        CLEAR_CONFIRM = new ConfigSectionHelper<String>(this, "clear-confirmation", "&4Warning! &7This command will &cclear the entire list&7! &7Repeat the &esame command &7to confirm this action.").getOrSet();

        LIST_MESSAGE = new ConfigSectionHelper<String>(this, "list.message", "&7Listed commands (&f%size%&7)&8: &f%commands%").getOrSet();
        LIST_SPLITTER = new ConfigSectionHelper<String>(this, "list.splitter", "&7, ").getOrSet();
        LIST_COMMAND = new ConfigSectionHelper<String>(this, "list.command", "&f%command%").getOrSet();

        ADD_SUCCESS = new ConfigSectionHelper<String>(this, "add.success", "&aSuccessfully added %command% into the list!").getOrSet();
        ADD_FAILED = new ConfigSectionHelper<String>(this, "add.failed", "&c%command% is already in the list!").getOrSet();

        REMOVE_SUCCESS = new ConfigSectionHelper<String>(this, "remove.success", "&aSuccessfully removed %command% from the list!").getOrSet();
        REMOVE_FAILED = new ConfigSectionHelper<String>(this, "remove.failed", "&c%command% is not listed!").getOrSet();
        if(!Reflection.isProxyServer()) return;

        CLEAR_SERVER = new ConfigSectionHelper<String>(this, "clear-server", "&aList has been cleared!").getOrSet();
        CLEAR_SERVER_CONFIRM = new ConfigSectionHelper<String>(this, "clear-confirmation-server", "&4Warning! &7This command will &cclear the entire list of this server&7! &7Repeat the &esame command &7to confirm this action.").getOrSet();

        LIST_SERVER_MESSAGE = new ConfigSectionHelper<String>(this, "list.message-server", "&7Listed commands of %server% (&f%size%&7)&8: &f%commands%").getOrSet();
        LIST_SERVER_SPLITTER = new ConfigSectionHelper<String>(this, "list.splitter-server", "&7, ").getOrSet();
        LIST_SERVER_COMMAND = new ConfigSectionHelper<String>(this, "list.command-server", "&f%command%").getOrSet();

        ADD_SERVER_SUCCESS = new ConfigSectionHelper<String>(this, "add.success-server", "&aSuccessfully added %command% into the list of %server%!").getOrSet();
        ADD_SERVER_FAILED = new ConfigSectionHelper<String>(this, "add.failed-server", "&c%command% is already in the list of %server%!").getOrSet();

        REMOVE_SERVER_SUCCESS = new ConfigSectionHelper<String>(this, "remove.success-server", "&aSuccessfully removed %command% from the list of %server%!").getOrSet();
        REMOVE_SERVER_FAILED = new ConfigSectionHelper<String>(this, "remove.failed-server", "&c%command% is in the list of %server%!").getOrSet();
    }
}
