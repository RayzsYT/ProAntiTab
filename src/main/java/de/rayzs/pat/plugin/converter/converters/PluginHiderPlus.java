package de.rayzs.pat.plugin.converter.converters;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PluginHiderPlus extends Converter {

    public PluginHiderPlus() {
        super("PluginHider+", "PluginHiderPlus", "config");
    }

    @Override
    public void apply(CommandSender sender) {

        boolean blacklist = (boolean) config.get("reverse-suggestionsWhitelist");
        Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.getConfig().setAndSave("turn-blacklist-to-whitelist", !blacklist);

        List<String> commands = (ArrayList<String>) config.get("suggestionsWhitelist");

        if (blacklist) {
            List<String> blockedCommands = (ArrayList<String>) config.get("blockedCommands.everyone.commands");
            commands.addAll(blockedCommands);
        }

        applyStorage(Storage.Blacklist.getBlacklist(), commands);
        sender.sendMessage("&e&lNotice: &7Since PluginHider+ both blocks certain commands and whitelist a few, the conversion into PAT's storage.yml format won't be accurate. Only whitelisted commands have been added, and blocked commands for everyone if the plugin was used in blacklist mode.");
    }
}
