package de.rayzs.pat.plugin.commands;

import de.rayzs.pat.plugin.process.CommandProcess;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.*;

public class  BungeeCommand extends Command implements TabExecutor {

    public BungeeCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        CommandProcess.handleCommand(commandSender, strings, "bpat");
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return CommandProcess.handleTabComplete(commandSender, strings);
    }
}
