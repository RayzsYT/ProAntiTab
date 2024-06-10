package de.rayzs.pat.plugin.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.plugin.process.CommandProcess;
import java.util.List;

public class VelocityCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player) {
            Player player = (Player) invocation.source();
            CommandProcess.handleCommand(player, invocation.arguments(), "bpat");
            return;
        }

        CommandProcess.handleCommand(invocation.source(), invocation.arguments(), "bpat");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return CommandProcess.handleTabComplete(invocation.source(), invocation.arguments());
    }
}
