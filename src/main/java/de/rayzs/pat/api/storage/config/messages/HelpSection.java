package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

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
                        , "&f  reload &7to reload the plugin"
                        , "&f  stats &7view when data has been synced"
                        , "&f  notify &7to get alerted"
                        , "&f  listgroups &7List all groups"
                        , "&f  creategroup (group) &7Create a group"
                        , "&f  deletegroup (group) &7Delete a group"
                        , "&f  list &8(optional: group) &7to see all listed commands"
                        , "&f  clear &8(optional: group) &7to clear the list"
                        , "&f  add/remove (command) &8(optional: group) &7to manage the list")
                :
                Arrays.asList(
                        "&7Available commands are: &f/%label%&7..."
                        , "&f  reload &7to reload the plugin"
                        , "&f  notify &7to get alerted"
                        , "&f  listgroups &7List all groups"
                        , "&f  creategroup (group) &7Create a group"
                        , "&f  deletegroup (group) &7Delete a group"
                        , "&f  list &8(optional: group) &7to see all listed commands"
                        , "&f  clear &8(optional: group) &7to clear the list"
                        , "&f  add/remove (command) &8(optional: group) &7to manage the list"
                ));
    }
}
