package de.rayzs.pat.plugin.command.impl;

import com.velocitypowered.api.command.SimpleCommand;
import de.rayzs.pat.plugin.command.CommandProcess;
import java.util.List;

public class VelocityCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandProcess.handleCommand(invocation.source(), invocation.arguments(), "bpat");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return CommandProcess.handleTabComplete(invocation.source(), invocation.arguments());
    }
}
