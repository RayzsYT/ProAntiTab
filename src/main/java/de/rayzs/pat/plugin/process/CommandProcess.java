package de.rayzs.pat.plugin.process;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.listeners.BukkitAntiTabListener;
import de.rayzs.pat.utils.*;
import java.util.*;

public class CommandProcess {

    public static void handleCommand(Object senderObj, String[] args, String label) {
        CommandSender sender = new CommandSender(senderObj);
        int length = args.length;
        String task, sub;
        boolean bool;
        if(!PermissionUtil.hasPermissionWithResponse(sender, "use")) return;

        try {
            switch (length) {
                case 1:
                    task = args[0].toLowerCase();

                    switch (task) {
                        case "reload":
                        case "rl":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "reload")) return;
                            sender.sendMessage(Storage.RELOAD_LOADING);
                            Storage.load();
                            if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
                            sender.sendMessage(Storage.RELOAD_DONE);
                            return;
                        case "clear": case "cls":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;
                            Storage.BLOCKED_COMMANDS_LIST.clear();
                            Storage.save();
                            if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
                            sender.sendMessage(Storage.BLACKLIST_CLEAR_MESSAGE);
                            return;
                        case "notify":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "notify")) return;
                            if(sender.isConsole()) {
                                bool = Storage.CONSOLE_NOTIFICATION_ENABLED;
                                Storage.CONSOLE_NOTIFICATION_ENABLED = !Storage.CONSOLE_NOTIFICATION_ENABLED;
                            } else {
                                bool = Storage.NOTIFY_PLAYERS.contains(sender.getUniqueId());
                                if(bool) Storage.NOTIFY_PLAYERS.remove(sender.getUniqueId());
                                else Storage.NOTIFY_PLAYERS.add(sender.getUniqueId());
                            }

                            sender.sendMessage(bool ? Storage.NOTIFY_DISABLED : Storage.NOTIFY_ENABLED);
                            return;
                        case "ls":
                        case "list":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "list")) return;
                            StringBuilder blacklistedCommandBuilder = new StringBuilder();
                            for (int i = 0; i < Storage.BLOCKED_COMMANDS_LIST.size(); i++) {
                                bool = (i >= Storage.BLOCKED_COMMANDS_LIST.size() - 1);
                                blacklistedCommandBuilder.append(Storage.BLACKLIST_LIST_COMMAND_MESSAGE).append(Storage.BLOCKED_COMMANDS_LIST.get(i));
                                if (!bool)
                                    blacklistedCommandBuilder.append(Storage.BLACKLIST_LIST_SPLITTER_MESSAGE);
                            }
                            sender.sendMessage(Storage.BLACKLIST_LIST_MESSAGE.replace("%size%", String.valueOf(Storage.BLOCKED_COMMANDS_LIST.size())).replace("%commands%", blacklistedCommandBuilder.toString()));
                            return;
                    }
                case 2:
                    task = args[0].toLowerCase();
                    sub = args[1];
                    bool = Storage.BLOCKED_COMMANDS_LIST.contains(sub);
                        switch (task) {
                            case "add":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;
                                if (!bool) {
                                    Storage.BLOCKED_COMMANDS_LIST.add(sub);
                                    Storage.save();
                                    if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
                                }
                                sender.sendMessage((bool ? Storage.BLACKLIST_ADD_FAIL_MESSAGE : Storage.BLACKLIST_ADD_MESSAGE).replace("%command%", sub));
                                return;
                            case "remove":
                            case "rem":
                            case "rm":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;
                                if (bool) {
                                    Storage.BLOCKED_COMMANDS_LIST.remove(sub);
                                    Storage.save();
                                    if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
                                }
                                sender.sendMessage((!bool ? Storage.BLACKLIST_REMOVE_FAIL_MESSAGE : Storage.BLACKLIST_REMOVE_MESSAGE).replace("%command%", sub));
                                return;
                        }

                default:
                    for (String line : Storage.COMMAND_HELP) sender.sendMessage(line.replace("&", "§").replace("%label%", label));
            }
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            sender.sendMessage(Storage.COMMAND_UNKNOWN);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static List<String> handleTabComplete(Object senderObj, String[] args) {
        CommandSender sender = new CommandSender(senderObj);
        final List<String> suggestions = new ArrayList<>(), result = new ArrayList<>();

        switch (args.length) {
            case 1:
                if (PermissionUtil.hasPermission(sender, "notify")) suggestions.add("notify");
                if (PermissionUtil.hasPermission(sender, "list")) suggestions.addAll(Arrays.asList("list", "ls"));
                if (PermissionUtil.hasPermission(sender, "clear")) suggestions.addAll(Arrays.asList("clear", "cls"));
                if (PermissionUtil.hasPermission(sender, "reload")) suggestions.addAll(Arrays.asList("reload", "rl"));
                if (PermissionUtil.hasPermission(sender, "add")) suggestions.add("add");
                if (PermissionUtil.hasPermission(sender, "remove")) suggestions.addAll(Arrays.asList("remove", "rem", "rm"));
                break;
            case 2:
                if(args[0].equals("add") && PermissionUtil.hasPermission(sender, "add") && !Reflection.isBungeecordServer()) suggestions.addAll(BukkitLoader.getNotBlockedCommands());
                if(Arrays.asList("remove", "rem", "rm").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove")) suggestions.addAll(Storage.BLOCKED_COMMANDS_LIST);
                break;
        }

        suggestions.stream().filter(suggestion -> suggestion.startsWith(args[args.length-1].toLowerCase())).forEach(result::add);
        return result;
    }
}
