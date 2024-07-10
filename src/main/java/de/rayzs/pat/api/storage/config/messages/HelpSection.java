package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import java.util.Arrays;

public class HelpSection extends ConfigStorage {

    public MultipleMessagesHelper MESSAGE;

    public HelpSection() {
        super("help");
    }

    @Override
    public void load() {
        super.load();
        MESSAGE = new MultipleMessagesHelper(this, null, Reflection.isProxyServer() ?
                Arrays.asList(
                        "&7Available commands are: &f/%label%&7..."
                        , "&7Format: &8<optional> (required)"
                        , "&f  reload &7to reload the plugin"
                        , "&f  notify &7to get alerted"
                        , "&f  info &7to get a few information"
                        , "&f  update &7to update all player permissions"
                        , "&f  creategroup (group) &7Create a group"
                        , "&f  deletegroup (group) &7Delete a group"
                        , "&f  setpriority (group) (priority) &7to set the priority"
                        , "&f  list &8(optional: group) &7to see all listed commands"
                        , "&f  listgroups &7List all groups"
                        , "&f  listpriorites &7to list all group priorities"
                        , "&f  add/rem (command) &8<group> &7to manage the list"
                        , "&f  clear &8<group> &7to clear the list")
                :
                Arrays.asList(
                        "&7Available commands are: &f/%label%&7..."
                        , "&7Format: &8<optional> (required)"
                        , "&f  reload &7to reload the plugin"
                        , "&f  notify &7to get alerted"
                        , "&f  info &7to get a few information"
                        , "&f  update &7to update all player permissions"
                        , "&f  creategroup (group) &7Create a group"
                        , "&f  deletegroup (group) &7Delete a group"
                        , "&f  setpriority (group) (priority) &7to set the priority"
                        , "&f  list &8<group> &7to see all listed commands"
                        , "&f  listgroups &8<server> &7List all groups"
                        , "&f  listpriorites &7to list all group priorities"
                        , "&f  add/rem (command) &8<group> &7to manage the list"
                        , "&f  clear &8<group> &7to clear the list"
                        , "&7 For a specific server:"
                        , "&f  serv list (server) &8<group>"
                        , "&f  serv add/rem (server) (command) &8<group>"
                        , "&f  serv clear (server) &8<group>"
                ));
    }
}
