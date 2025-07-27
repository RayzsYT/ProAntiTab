package de.rayzs.pat.plugin.commands;

import de.rayzs.pat.plugin.process.CommandProcess;
import org.bukkit.entity.Player;
import org.bukkit.command.*;
import java.util.List;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            CommandProcess.handleCommand(player, strings, label);
            return true;
        }

        CommandProcess.handleCommand(commandSender, strings, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] strings) {
        return CommandProcess.handleTabComplete(commandSender, strings);
    }
}
