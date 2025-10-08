package de.rayzs.pat.plugin.process;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.plugin.process.impl.local.info.*;
import de.rayzs.pat.plugin.process.impl.local.modify.*;
import de.rayzs.pat.plugin.process.impl.local.modify.list.ListCommand;
import de.rayzs.pat.plugin.process.impl.local.modify.list.ListGroupsCommand;
import de.rayzs.pat.plugin.process.impl.local.modify.list.ListPrioritiesCommand;
import de.rayzs.pat.plugin.process.impl.local.system.NotifyCommand;
import de.rayzs.pat.plugin.process.impl.local.system.PostDebugCommand;
import de.rayzs.pat.plugin.process.impl.local.system.ReloadCommand;
import de.rayzs.pat.plugin.process.impl.local.system.UpdateCommand;
import de.rayzs.pat.plugin.process.impl.server.ServAddCommand;
import de.rayzs.pat.plugin.process.impl.server.ServClearCommand;
import de.rayzs.pat.plugin.process.impl.server.ServRemoveCommand;
import de.rayzs.pat.plugin.process.impl.server.list.ServListCommand;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.storage.Storage;

import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;

import java.util.*;

public class CommandProcess {

    private static final List<ProCommand> COMMANDS = new ArrayList<>();

    public static void initialize() {
        COMMANDS.add(new SetPriorityCommand());
        COMMANDS.add(new NotifyCommand());

        COMMANDS.add(new AddCommand());
        COMMANDS.add(new RemoveCommand());
        COMMANDS.add(new ClearCommand());

        COMMANDS.add(new CreateGroupCommand());
        COMMANDS.add(new DeleteGroupCommand());

        COMMANDS.add(new ListCommand());
        COMMANDS.add(new ListGroupsCommand());
        COMMANDS.add(new ListPrioritiesCommand());

        COMMANDS.add(new StatsCommand());
        COMMANDS.add(new InfoCommand());
        COMMANDS.add(new PermsCommand());

        COMMANDS.add(new PostDebugCommand());
        COMMANDS.add(new UpdateCommand());
        COMMANDS.add(new ReloadCommand());

        // PROXY
        COMMANDS.add(new ServListCommand());

        COMMANDS.add(new ServAddCommand());
        COMMANDS.add(new ServRemoveCommand());
        COMMANDS.add(new ServClearCommand());
    }

    public static void handleCommand(Object senderObj, String[] args, String label) {
        CommandSender sender = CommandSenderHandler.from(senderObj);

        if (!PermissionUtil.hasPermissionWithResponse(sender, "use"))
            return;

        if (args.length == 0) {

            Storage.ConfigSections.Messages.HELP.MESSAGE.getLines().forEach(line -> {
                sender.sendMessage(line.replace("%label%", label));
            });

            return;
        }

        String commandName = args[0];
        String[] commandArgs = new String[] {};

        if (commandName.equals("serv") || commandName.equals("server")) {

            if (args.length < 2) {
                sender.sendMessage(Storage.ConfigSections.Messages.COMMAND_FAILED.MESSAGE);
                return;
            }

            String servCommandName = StringUtils.getFirstArg(args[1]);

            if (args.length > 2) {
                commandArgs = Arrays.copyOfRange(args, 2, args.length);
            }

            Optional<ProCommand> optCommand = COMMANDS.stream().filter(cmd -> {
                if (Reflection.isProxyServer() && cmd.isServerCommand())
                    return cmd.isCommand(servCommandName);

                return false;
            }).findFirst();

            if (optCommand.isEmpty()) {
                sender.sendMessage(Storage.ConfigSections.Messages.COMMAND_FAILED.MESSAGE);
                return;
            }

            ProCommand command = optCommand.get();
            if (!PermissionUtil.hasPermissionWithResponse(sender, command.getName()))
                return;

            if (!command.execute(sender, commandArgs)) {
                sender.sendMessage(Storage.ConfigSections.Messages.COMMAND_FAILED.MESSAGE);
            }

            return;
        }

        if (args.length > 1) {
            commandArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        Optional<ProCommand> optCommand = COMMANDS.stream().filter(cmd -> {

            if (!Reflection.isProxyServer() && !cmd.isProxyOnly() || Reflection.isProxyServer() && !cmd.isServerCommand())
                return cmd.isCommand(commandName);

            return false;
        }).findFirst();

        if (optCommand.isEmpty()) {
            sender.sendMessage(Storage.ConfigSections.Messages.COMMAND_FAILED.MESSAGE);
            return;
        }

        ProCommand command = optCommand.get();
        if (!PermissionUtil.hasPermissionWithResponse(sender, command.getName()))
            return;

        if (!command.execute(sender, commandArgs)) {
            sender.sendMessage(Storage.ConfigSections.Messages.COMMAND_FAILED.MESSAGE);
        }

    }

    public static List<String> handleTabComplete(Object senderObj, String[] args) {
        CommandSender sender = CommandSenderHandler.from(senderObj);
        List<String> result = new ArrayList<>();

        if (!PermissionUtil.hasPermission(sender, "use"))
            return result;

        if (args.length <= 1) {

            if (Reflection.isProxyServer())
                result.addAll(Arrays.asList("serv", "server"));

            COMMANDS.stream()
                    .filter(cmd -> {

                        if (!Reflection.isProxyServer() && cmd.isProxyOnly())
                            return false;

                        return PermissionUtil.hasPermission(sender, cmd.getName());
                    })
                    .forEach(cmd -> {
                        result.add(cmd.getName());
                        result.addAll(cmd.getAliases());
                    });

        } else {

            String commandName = StringUtils.getFirstArg(args[0]);
            String[] commandArgs;
            Optional<ProCommand> optCommand;

            if (commandName.equals("serv") || commandName.equals("server")) {

                if (args.length == 2) {

                    COMMANDS.stream()
                            .filter(cmd -> {

                                if (Reflection.isProxyServer() && cmd.isServerCommand())
                                    return PermissionUtil.hasPermission(sender, cmd.getName());

                                return false;
                            })
                            .forEach(cmd -> {
                                result.add(cmd.getName());
                                result.addAll(cmd.getAliases());
                            });

                    return result;
                }

                String servCommandName = StringUtils.getFirstArg(args[1]);
                commandArgs = Arrays.copyOfRange(args, 2, args.length);

                if (commandArgs.length == 1 && commandArgs[0].isBlank())
                    commandArgs = new String[] {};

                optCommand = COMMANDS.stream().filter(cmd -> {

                    if (Reflection.isProxyServer() && cmd.isServerCommand()) {
                        return cmd.isCommand(servCommandName) && PermissionUtil.hasPermission(sender, cmd.getName());
                    }

                    return false;
                }).findFirst();

            } else {

                commandArgs = Arrays.copyOfRange(args, 1, args.length);

                if (commandArgs.length == 1 && commandArgs[0].isBlank())
                    commandArgs = new String[]{};

                optCommand = COMMANDS.stream().filter(cmd -> {
                    if (!Reflection.isProxyServer() && cmd.isProxyOnly() || cmd.isServerCommand()) {
                        return false;
                    }

                    return cmd.isCommand(commandName);
                }).findFirst();
            }

            if (optCommand.isPresent()) {
                ProCommand command = optCommand.get();

                if (PermissionUtil.hasPermission(sender, command.getName())) {
                    List<String> cmdCompletions = command.tabComplete(sender, commandArgs);
                    result.addAll(cmdCompletions != null ? cmdCompletions : List.of());
                }

            }
        }

        return result.stream().filter(suggestion -> {

            if (args.length == 0)
                return true;

            String command = args[Math.max(0, args.length-1)].toLowerCase();
            suggestion = suggestion.toLowerCase();

            if (command.startsWith("\"")) {
                command = command.length() == 1 ? "" : command.substring(1);
            }

            return suggestion.startsWith(command);
        }).toList();
    }
}
