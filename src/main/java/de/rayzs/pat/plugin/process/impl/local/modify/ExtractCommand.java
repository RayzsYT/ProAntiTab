package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;
import java.util.*;

public class ExtractCommand extends ProCommand {

    public ExtractCommand() {
        super(
                "extract",
                ""
        );
    }

    private enum Mode {

        BOTH("both", true), ONLY_COLON("only-colon", true), NON_COLON("non-colon", false);

        private static Mode getMode(final String name) {
            for (final Mode mode : Mode.values()) {
                if (mode.getName().equalsIgnoreCase(name)) {
                    return mode;
                }
            }

            return null;
        }

        public String getName() {
            return name;
        }

        public boolean doesIncludesColons() {
            return includesColons;
        }

        private final String name;
        private final boolean includesColons;

        Mode(final String name, final boolean includesColons) {
            this.name = name;
            this.includesColons = includesColons;
        }
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(Storage.ConfigSections.Messages.EXTRACT.USAGE);
            return true;
        }

        Mode mode = Mode.NON_COLON;

        String pluginName = args[0].toLowerCase();
        boolean failed = false;
        Group group = null;


        if (args.length == 2) {

            group = GroupManager.getGroupByName(args[1]);

            if (group == null) {
                mode = Mode.getMode(args[1]);
                failed = mode == null;
            }

        } else if (args.length == 3) {

            group = GroupManager.getGroupByName(args[1]);
            mode = Mode.getMode(args[2]);

            if (group == null || mode == null) {
                failed = true;
            }
        }

        if (failed) {
            sender.sendMessage(Storage.ConfigSections.Messages.EXTRACT.USAGE);
            return true;
        }

        List<String> pluginNames = Storage.getLoader().getPluginNames("%n");
        boolean pluginFound = false, all = pluginName.equalsIgnoreCase("*");

        for (String name : pluginNames) {
            if (name.equalsIgnoreCase(pluginName)) {
                pluginFound = true;
                break;
            }
        }

        if (!all && !pluginFound) {
            sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.EXTRACT.PLUGIN_NOT_FOUND,
                    "%plugin%", pluginName)
            );

            return true;
        }

        List<String> commands = all
                ? Storage.getLoader().getAllCommands(mode.doesIncludesColons())
                : Storage.getLoader().getPluginCommands(args[0], mode.doesIncludesColons());

        if (mode == Mode.ONLY_COLON) {
            commands = commands.stream().filter(command -> command.contains(":")).toList();
        }

        BlacklistStorage storage = group == null
                ? Storage.Blacklist.getBlacklist()
                : group.getGeneralGroupBlacklist();

        commands = commands.stream().filter(command -> !storage.isListed(command)).toList();
        commands.forEach(storage::add);

        storage.save();

        sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.EXTRACT.SUCCESS,
                "%amount%", String.valueOf(commands.size()),
                "%plugin%", pluginName
        ));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        final int length = args.length;

        if (length <= 1) {
            final List<String> suggestions =  Storage.getLoader().getPluginNames("%n");
            suggestions.add("*");

            return suggestions;
        }

        List<String> result = new ArrayList<>();

        if (length == 2 || length == 3 && GroupManager.getGroupByName(args[1]) != null) {
            result.addAll(Arrays.stream(Mode.values()).map(Mode::getName).toList());
        }

        if (length == 2) {
            result.addAll(GroupManager.getGroupNames());
        }

        return result;
    }
}
