package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class GroupSection extends ConfigStorage {

    public String CREATE, ALREADY_EXIST, DOES_NOT_EXIST, DELETE, DELETE_CONFIRM, CREATE_SERVER, ALREADY_SERVER_EXIST, DOES_NOT_EXIST_SERVER, DELETE_SERVER, DELETE_SERVER_CONFIRM;
    public String CLEAR, CLEAR_CONFIRM, CLEAR_SERVER, CLEAR_SERVER_CONFIRM;

    public String LIST_MESSAGE, LIST_SPLITTER, LIST_COMMAND, LIST_SERVER_MESSAGE, LIST_SERVER_SPLITTER, LIST_SERVER_COMMAND;
    public String LIST_GROUP_MESSAGE, LIST_GROUP_SPLITTER, LIST_GROUP_GROUPS, LIST_GROUP_SERVER_MESSAGE, LIST_GROUP_SERVER_SPLITTER, LIST_GROUP_SERVER_GROUPS;

    public String ADD_SUCCESS, ADD_FAILED;
    public String REMOVE_SUCCESS, REMOVE_FAILED;

    public String ADD_SERVER_SUCCESS, ADD_SERVER_FAILED;
    public String REMOVE_SERVER_SUCCESS, REMOVE_SERVER_FAILED;

    public GroupSection() {
        super("group");
    }

    @Override
    public void load() {
        super.load();

        CREATE = new ConfigSectionHelper<String>(this, "create", "&aGroup %group% has been created!").getOrSet();
        ALREADY_EXIST = new ConfigSectionHelper<String>(this, "already-exist", "&cGroup %group% already exist!").getOrSet();
        DOES_NOT_EXIST = new ConfigSectionHelper<String>(this, "does-not-exist", "&cGroup %group% does not exist!").getOrSet();
        DELETE = new ConfigSectionHelper<String>(this, "delete", "&cGroup %group% has been deleted!").getOrSet();
        DELETE_CONFIRM = new ConfigSectionHelper<String>(this, "delete-confirmation", "&4Warning! &7This command will &cdelete the group with the whole list&7 of this group! &7Repeat the &esame command &7to confirm this action.").getOrSet();

        CLEAR = new ConfigSectionHelper<String>(this, "clear", "&aList of group %group% has been cleared!").getOrSet();
        CLEAR_CONFIRM = new ConfigSectionHelper<String>(this, "clear-confirmation", "&4Warning! &7This command will &cclear the entire list&7 of this group! &7Repeat the &esame command &7to confirm this action.").getOrSet();

        LIST_MESSAGE = new ConfigSectionHelper<String>(this, "list.message", "&7Listed commands of group %group% (&f%size%&7)&8: &f%commands%").getOrSet();
        LIST_SPLITTER = new ConfigSectionHelper<String>(this, "list.splitter", "&7, ").getOrSet();
        LIST_COMMAND = new ConfigSectionHelper<String>(this, "list.command", "&f").getOrSet();

        LIST_GROUP_MESSAGE = new ConfigSectionHelper<String>(this, "list-groups.message", "&7All groups (&f%size%&7)&8: &f%groups%").getOrSet();
        LIST_GROUP_SPLITTER = new ConfigSectionHelper<String>(this, "list-groups.splitter", "&7, ").getOrSet();
        LIST_GROUP_GROUPS = new ConfigSectionHelper<String>(this, "list-groups.command", "&f").getOrSet();

        ADD_SUCCESS = new ConfigSectionHelper<String>(this, "add.success", "&aSuccessfully added %command% into the list of group %group%!").getOrSet();
        ADD_FAILED = new ConfigSectionHelper<String>(this, "add.failed", "&c%command% is already in the list of group %group%!").getOrSet();

        REMOVE_SUCCESS = new ConfigSectionHelper<String>(this, "remove.success", "&aSuccessfully removed %command% from the list of group %group%!").getOrSet();
        REMOVE_FAILED = new ConfigSectionHelper<String>(this, "remove.failed", "&c%command% is not listed in the group %group%!").getOrSet();
        if(!Reflection.isProxyServer()) return;

        CREATE_SERVER = new ConfigSectionHelper<String>(this, "create-server", "&aGroup %group% for %server% has been created!").getOrSet();
        ALREADY_SERVER_EXIST = new ConfigSectionHelper<String>(this, "already-exist-server", "&cGroup %group% for %server% already exist!").getOrSet();
        DOES_NOT_EXIST_SERVER = new ConfigSectionHelper<String>(this, "does-not-exist-server", "&cGroup %group% of %server% does not exist!").getOrSet();
        DELETE_SERVER = new ConfigSectionHelper<String>(this, "delete-server", "&cGroup %group% of %server% has been deleted!").getOrSet();
        DELETE_SERVER_CONFIRM = new ConfigSectionHelper<String>(this, "delete-confirmation-server", "&4Warning! &7This command will &cdelete the group of the server %server% with the whole list&7 of this group! &7Repeat the &esame command &7to confirm this action.").getOrSet();

        CLEAR_SERVER = new ConfigSectionHelper<String>(this, "clear-server", "&aList of group %group% from %server% has been cleared!").getOrSet();
        CLEAR_SERVER_CONFIRM = new ConfigSectionHelper<String>(this, "clear-confirmation-server", "&4Warning! &7This command will &cclear the entire list&7 of this group for %server%! &7Repeat the &esame command &7to confirm this action.").getOrSet();

        LIST_SERVER_MESSAGE = new ConfigSectionHelper<String>(this, "list.message-server", "&7Listed commands of group %group% from %server% (&f%size%&7)&8: &f%commands%").getOrSet();
        LIST_SERVER_SPLITTER = new ConfigSectionHelper<String>(this, "list.splitter-server", "&7, ").getOrSet();
        LIST_SERVER_COMMAND = new ConfigSectionHelper<String>(this, "list.command-server", "&f%group%").getOrSet();

        LIST_GROUP_SERVER_MESSAGE = new ConfigSectionHelper<String>(this, "list-groups.message-server", "&7All groups from %server% (&f%size%&7)&8: &f%groups%").getOrSet();
        LIST_GROUP_SERVER_SPLITTER = new ConfigSectionHelper<String>(this, "list-groups.splitter-server", "&7, ").getOrSet();
        LIST_GROUP_SERVER_GROUPS = new ConfigSectionHelper<String>(this, "list-groups.command-server", "&f%group%").getOrSet();

        ADD_SERVER_SUCCESS = new ConfigSectionHelper<String>(this, "add.success-server", "&aSuccessfully added %command% into the list of group %group% for %server%!").getOrSet();
        ADD_SERVER_FAILED = new ConfigSectionHelper<String>(this, "add.failed-server", "&c%command% is already in the list of group %group% of %server%!").getOrSet();

        REMOVE_SERVER_SUCCESS = new ConfigSectionHelper<String>(this, "remove.success-server", "&aSuccessfully removed %command% from the list of group %group% from %server%!").getOrSet();
        REMOVE_SERVER_FAILED = new ConfigSectionHelper<String>(this, "remove.failed-server", "&c%command% is not listed in the group %group% of %server%!").getOrSet();

    }
}
