package de.rayzs.pat.plugin.process;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.plugin.brand.CustomServerBrand;
import de.rayzs.pat.plugin.communication.ClientCommunication;
import de.rayzs.pat.plugin.communication.ClientInfo;
import de.rayzs.pat.utils.group.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandProcess {

    private static final ExpireCache<UUID, String> CONFIRMATION = new ExpireCache<>(4, TimeUnit.SECONDS);

    public static void handleCommand(Object senderObj, String[] args, String label) {
        CommandSender sender = new CommandSender(senderObj);
        UUID uuid = sender.getUniqueId();
        int length = args.length;
        String task, sub, extra, confirmationString;
        Group group;
        boolean bool, backend = Storage.BUNGEECORD && !Reflection.isProxyServer();

        if(!PermissionUtil.hasPermissionWithResponse(sender, "use")) return;

        try {
            switch (length) {
                case 1:
                    task = args[0].toLowerCase();

                    switch (task) {

                        case "stats":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "stats")) return;
                            if(!Reflection.isProxyServer()) {
                                sender.sendMessage(Storage.STATS_FAIL_MESSAGE);
                                return;
                            }

                            int found = 0;
                            StringBuilder statsBuilder = new StringBuilder();
                            for (int i = 0; i < ClientCommunication.CLIENTS.size(); i++) {
                                bool = (i >= ClientCommunication.CLIENTS.size() - 1);
                                ClientInfo client = ClientCommunication.CLIENTS.get(i);
                                if(!client.hasSentFeedback()) continue;
                                found++;

                                statsBuilder.append(Storage.STATS_SERVERS_MESSAGE.replace("%updated%", client.getSyncTime()).replace("%servername%", client.getName()));
                                if (!bool)
                                    statsBuilder.append(Storage.STATS_SERVERS_SPLITTER_MESSAGE);
                            }

                            int finalFound = found;
                            Storage.STATS.forEach(message -> sender.sendMessage(message.replace("%server_count%", String.valueOf(Storage.SERVER_DATA_SYNC_COUNT)).replace("%last_sync_time%", TimeConverter.calcAndGetTime(Storage.LAST_DATA_UPDATE)).replace("%servers%", finalFound > 0 ? statsBuilder : Storage.STATS_SERVERS_NO_SERVER_MESSAGE)));
                            return;

                        case "reload":
                        case "rl":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "reload")) return;
                            sender.sendMessage(Storage.RELOAD_LOADING);
                            Storage.load(!backend);
                            CustomServerBrand.initialize();

                            if(!backend) handleChange();
                            sender.sendMessage(Storage.RELOAD_DONE);
                            return;
                        case "clear":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;

                            if(Storage.BUNGEECORD && !Reflection.isProxyServer()) {
                                sender.sendMessage(Storage.BUNGEECORD_MESSAGE);
                                return;
                            }

                            confirmationString = "clear";

                            if(CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                Storage.BLOCKED_COMMANDS_LIST.clear();
                                Storage.saveStorage();
                                handleChange();
                                sender.sendMessage(Storage.BLACKLIST_CLEAR_MESSAGE);
                            } else {
                                CONFIRMATION.put(uuid, confirmationString);
                                sender.sendMessage(Storage.BLACKLIST_CLEAR_CONFIRM_MESSAGE);
                            }
                            return;
                        case "notify":

                            if(!PermissionUtil.hasPermissionWithResponse(sender, "notify")) return;

                            if(backend) {
                                sender.sendMessage(Storage.BUNGEECORD_MESSAGE);
                                return;
                            }

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

                            if(backend) {
                                sender.sendMessage(Storage.BUNGEECORD_MESSAGE);
                                return;
                            }

                            StringBuilder blacklistedCommandBuilder = new StringBuilder();
                            for (int i = 0; i < Storage.BLOCKED_COMMANDS_LIST.size(); i++) {
                                bool = (i >= Storage.BLOCKED_COMMANDS_LIST.size() - 1);
                                blacklistedCommandBuilder.append(Storage.BLACKLIST_LIST_COMMAND_MESSAGE).append(Storage.BLOCKED_COMMANDS_LIST.get(i));
                                if (!bool)
                                    blacklistedCommandBuilder.append(Storage.BLACKLIST_LIST_SPLITTER_MESSAGE);
                            }
                            sender.sendMessage(Storage.BLACKLIST_LIST_MESSAGE.replace("%size%", String.valueOf(Storage.BLOCKED_COMMANDS_LIST.size())).replace("%commands%", blacklistedCommandBuilder.toString()));
                            return;

                        case "listgroups":
                        case "lg":

                            if(!PermissionUtil.hasPermissionWithResponse(sender, "listgroups")) return;

                            if(backend) {
                                sender.sendMessage(Storage.BUNGEECORD_MESSAGE);
                                return;
                            }

                            StringBuilder groupsBuilder = new StringBuilder();
                            for (int i = 0; i < GroupManager.getGroupNames().size(); i++) {
                                bool = (i >= GroupManager.getGroupNames().size() - 1);
                                groupsBuilder.append(Storage.GROUPS_LIST_GROUPS_MESSAGE).append(GroupManager.getGroupNames().get(i));
                                if (!bool)
                                    groupsBuilder.append(Storage.GROUPS_LIST_SPLITTER_MESSAGE);
                            }
                            sender.sendMessage(Storage.GROUPS_LIST_MESSAGE.replace("%size%", String.valueOf(GroupManager.getGroupNames().size())).replace("%groups%", groupsBuilder.toString()));
                            return;
                    }
                case 2:

                    if(backend) {
                        sender.sendMessage(Storage.BUNGEECORD_MESSAGE);
                        return;
                    }

                    task = args[0].toLowerCase();
                    sub = args[1];
                    bool = Storage.BLOCKED_COMMANDS_LIST.contains(sub);
                        switch (task) {

                            case "ls":
                            case "list":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "list")) return;
                                group = GroupManager.getGroupByName(sub.toLowerCase());

                                if(group == null) {
                                    sender.sendMessage(Storage.GROUP_NOT_EXIST_MESSAGE.replace("%group%", sub));
                                    return;
                                }

                                StringBuilder blacklistedCommandBuilder = new StringBuilder();
                                for (int i = 0; i < group.getCommands().size(); i++) {
                                    bool = (i >= group.getCommands().size() - 1);
                                    blacklistedCommandBuilder.append(Storage.GROUP_LIST_COMMAND_MESSAGE).append(group.getCommands().get(i));
                                    if (!bool)
                                        blacklistedCommandBuilder.append(Storage.GROUP_LIST_SPLITTER_MESSAGE);
                                }
                                sender.sendMessage(Storage.GROUP_LIST_MESSAGE.replace("%size%", String.valueOf(group.getCommands().size())).replace("%commands%", blacklistedCommandBuilder.toString()).replace("%group%", group.getGroupName()));
                                return;

                            case "creategroup":
                            case "cg":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "creategroup")) return;
                                if(sub.equalsIgnoreCase("commands")) return;
                                bool = GroupManager.isGroupRegistered(sub.toLowerCase());

                                if(!bool) GroupManager.registerGroup(sub.toLowerCase());
                                sender.sendMessage((bool ? Storage.GROUP_ALREADY_CREATED_MESSAGE : Storage.GROUP_CREATE_MESSAGE).replace("%group%", sub.toLowerCase()));
                                return;

                            case "deletegroup":
                            case "dg":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "deletegroup")) return;
                                bool = GroupManager.isGroupRegistered(sub.toLowerCase());
                                confirmationString = "deletegroup " + sub.toLowerCase();

                                if(CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                    if (bool) GroupManager.unregisterGroup(sub.toLowerCase());
                                    sender.sendMessage((bool ? Storage.GROUP_DELETE_MESSAGE : Storage.GROUP_NOT_EXIST_MESSAGE).replace("%group%", sub.toLowerCase()));
                                } else {
                                    CONFIRMATION.put(uuid, confirmationString);
                                    sender.sendMessage(Storage.GROUP_DELETE_CONFIRM_MESSAGE);
                                }
                                return;

                            case "clear":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;
                                group = GroupManager.getGroupByName(sub);

                                if(group == null) {
                                    sender.sendMessage(Storage.GROUP_NOT_EXIST_MESSAGE.replace("%group%", sub));
                                    return;
                                }
                                confirmationString = "clear " + group.getGroupName();

                                if(CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                    group.clear();
                                    handleChange();

                                    sender.sendMessage(Storage.GROUP_CLEAR_MESSAGE.replace("%group%", group.getGroupName()));
                                } else {
                                    CONFIRMATION.put(uuid, confirmationString);
                                    sender.sendMessage(Storage.GROUP_CLEAR_CONFIRM_MESSAGE);
                                }
                                return;

                            case "add":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;
                                if (!bool) {
                                    Storage.BLOCKED_COMMANDS_LIST.add(sub);
                                    Storage.saveStorage();
                                    handleChange();
                                }

                                sender.sendMessage((bool ? Storage.BLACKLIST_ADD_FAIL_MESSAGE : Storage.BLACKLIST_ADD_MESSAGE).replace("%command%", sub));
                                return;
                            case "remove":
                            case "rem":
                            case "rm":
                                if(!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;
                                if (bool) {
                                    Storage.BLOCKED_COMMANDS_LIST.remove(sub);
                                    Storage.saveStorage();
                                    handleChange();
                                }
                                sender.sendMessage((!bool ? Storage.BLACKLIST_REMOVE_FAIL_MESSAGE : Storage.BLACKLIST_REMOVE_MESSAGE).replace("%command%", sub));
                                return;
                        }

                case 3:

                    if(backend) {
                        sender.sendMessage(Storage.BUNGEECORD_MESSAGE);
                        return;
                    }

                    task = args[0].toLowerCase();
                    sub = args[1].toLowerCase();
                    extra = args[2].toLowerCase();
                    group = GroupManager.getGroupByName(extra);
                    if(group == null) {
                        sender.sendMessage(Storage.GROUP_NOT_EXIST_MESSAGE.replace("%group%", extra));
                        return;
                    }

                    extra = group.getGroupName();

                    bool = group.contains(sub);
                    switch (task) {

                        case "add":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;
                            if (!bool) {
                                group.add(sub);
                                handleChange();
                            }

                            sender.sendMessage((bool ? Storage.GROUP_ADD_FAIL_MESSAGE : Storage.GROUP_ADD_MESSAGE).replace("%command%", sub).replace("%group%", extra));
                            return;
                        case "remove":
                        case "rem":
                        case "rm":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;
                            if (bool) {
                                group.remove(sub);
                                handleChange();
                            }
                            sender.sendMessage((!bool ? Storage.GROUP_REMOVE_FAIL_MESSAGE : Storage.GROUP_REMOVE_MESSAGE).replace("%command%", sub).replace("%group%", extra));
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
        final boolean backend = Storage.BUNGEECORD && !Reflection.isProxyServer();

        switch (args.length) {
            case 1:
                if (!backend && PermissionUtil.hasPermission(sender, "stats") && Reflection.isProxyServer()) suggestions.add("stats");
                if (!backend && PermissionUtil.hasPermission(sender, "notify")) suggestions.add("notify");
                if (!backend && PermissionUtil.hasPermission(sender, "creategroup")) suggestions.addAll(Arrays.asList("creategroup", "cg"));
                if (!backend && PermissionUtil.hasPermission(sender, "deletegroup")) suggestions.addAll(Arrays.asList("deletegroup", "dg"));
                if (!backend && PermissionUtil.hasPermission(sender, "deletegroup")) suggestions.addAll(Arrays.asList("listgroups", "lg"));
                if (!backend && PermissionUtil.hasPermission(sender, "list")) suggestions.addAll(Arrays.asList("list", "ls"));
                if (!backend && PermissionUtil.hasPermission(sender, "clear")) suggestions.add("clear");
                if (PermissionUtil.hasPermission(sender, "reload")) suggestions.addAll(Arrays.asList("reload", "rl"));
                if (!backend && PermissionUtil.hasPermission(sender, "add")) suggestions.add("add");
                if (!backend && PermissionUtil.hasPermission(sender, "remove")) suggestions.addAll(Arrays.asList("remove", "rem", "rm"));
                break;
            case 2:
                if(!backend && Arrays.asList("deletegroup", "dg").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "deletegroup")) suggestions.addAll(GroupManager.getGroupNames());
                if(!backend && args[0].equals("clear") && PermissionUtil.hasPermission(sender, "clear")) suggestions.addAll(GroupManager.getGroupNames());
                if(!backend && Arrays.asList("ls", "list").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "list")) suggestions.addAll(GroupManager.getGroupNames());
                if(!backend && args[0].equals("add") && PermissionUtil.hasPermission(sender, "add") && !Reflection.isProxyServer()) suggestions.addAll(BukkitLoader.getNotBlockedCommands());
                if(!backend && Arrays.asList("remove", "rem", "rm").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove")) suggestions.addAll(Storage.BLOCKED_COMMANDS_LIST);
                break;
            case 3:
                if(!backend && args[0].equals("add") && PermissionUtil.hasPermission(sender, "add") && !Reflection.isProxyServer()) suggestions.addAll(GroupManager.getGroupByNameNotIncludingCommand(args[1]));
                if(!backend && Arrays.asList("remove", "rem", "rm").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove")) suggestions.addAll(GroupManager.getGroupByNameOnlyIncludingCommand(args[1]));
                break;
        }

        suggestions.stream().filter(suggestion -> suggestion.startsWith(args[args.length-1].toLowerCase())).forEach(result::add);
        return result;
    }

    private static void handleChange() {
        if(Reflection.isProxyServer()) {
            ClientCommunication.synchronizeInformation();
            if(Reflection.isVelocityServer()) VelocityAntiTabListener.updateCommands();
        } else {
            if (Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
        }
    }
}
