package de.rayzs.pat.plugin.commands;

import de.rayzs.pat.plugin.process.CommandProcess;
import org.bukkit.command.*;

import java.util.List;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandProcess.handleCommand(commandSender, strings, s);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return CommandProcess.handleTabComplete(commandSender, strings);
    }
}
