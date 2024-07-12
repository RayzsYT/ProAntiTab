package de.rayzs.pat.plugin.process;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.plugin.listeners.bungee.WaterfallAntiTabListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import java.util.concurrent.TimeUnit;
import de.rayzs.pat.utils.group.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class CommandProcess {

    private static final ExpireCache<UUID, String> CONFIRMATION = new ExpireCache<>(4, TimeUnit.SECONDS);

    public static void handleCommand(Object senderObj, String[] args, String label) {

        CommandSender sender = new CommandSender(senderObj);

        UUID uuid = sender.getUniqueId();
        int length = args.length;
        String task, sub, extra, subExtra, confirmationString, stringList;
        Group group;
        boolean bool, backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

        if(!PermissionUtil.hasPermissionWithResponse(sender, "use")) return;

        for (String arg : args) if(arg.contains(".") || arg.contains("\"") || arg.contains("'")) return;

        try {
            switch (length) {
                case 1:
                    task = args[0].toLowerCase();

                    switch (task) {

                        case "stats":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "stats")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            int found = 0;
                            StringBuilder statsBuilder = new StringBuilder();
                            for (int i = 0; i < Communicator.CLIENTS.size(); i++) {
                                bool = (i >= Communicator.CLIENTS.size() - 1);
                                ClientInfo client = Communicator.CLIENTS.get(i);
                                if(!client.hasSentFeedback()) continue;
                                found++;

                                statsBuilder.append(Storage.ConfigSections.Messages.STATS.SERVER.replace("%updated%", client.getSyncTime()).replace("%servername%", client.getName()));
                                if (!bool)
                                    statsBuilder.append(Storage.ConfigSections.Messages.STATS.SPLITTER);
                            }

                            int finalFound = found;
                            Storage.ConfigSections.Messages.STATS.STATISTIC.getLines().forEach(message -> sender.sendMessage(message.replace("%groups_amount%", String.valueOf(GroupManager.getGroupNames().size())).replace("%server_count%", String.valueOf(Communicator.SERVER_DATA_SYNC_COUNT)).replace("%last_sync_time%", TimeConverter.calcAndGetTime(Communicator.LAST_DATA_UPDATE)).replace("%servers%", finalFound > 0 ? statsBuilder : Storage.ConfigSections.Messages.STATS.NO_SERVER)));
                            return;

                        case "reload":
                        case "rl":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "reload")) return;
                            sender.sendMessage(Storage.ConfigSections.Messages.RELOAD.LOADING);

                            Storage.loadAll(!backend);
                            CustomServerBrand.initialize();
                            GroupManager.clearAllGroups();
                            GroupManager.initialize();
                            handleReload();

                            if(!backend) handleChange();
                            sender.sendMessage(Storage.ConfigSections.Messages.RELOAD.DONE);
                            return;

                        case "info":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "info")) return;

                            sender.sendMessage(
                                    StringUtils.buildStringList(Storage.ConfigSections.Messages.INFO.MESSAGE.getLines())
                                            .replace("%sync_time%", Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED || Reflection.isProxyServer() ? Storage.ConfigSections.Messages.INFO.SYNC_TIME.replace("%time%", TimeConverter.calcAndGetTime(Reflection.isProxyServer() ? Communicator.LAST_DATA_UPDATE : Storage.LAST_SYNC)) : Storage.ConfigSections.Messages.INFO.SYNC_DISABLED)
                                            .replace("%version_status%", Storage.OUTDATED ? Storage.ConfigSections.Messages.INFO.VERSION_OUTDATED : Storage.ConfigSections.Messages.INFO.VERSION_UPDATED)
                            );

                            return;

                        case "clear":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            confirmationString = "clear";

                            if(CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                Storage.Blacklist.getBlacklist().clear().save();
                                handleChange();
                                sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR);
                            } else {
                                CONFIRMATION.put(uuid, confirmationString);
                                sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR_CONFIRM);
                            }
                            return;

                        case "notify":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "notify")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            if(sender.isConsole()) {
                                bool = Storage.SEND_CONSOLE_NOTIFICATION;
                                Storage.SEND_CONSOLE_NOTIFICATION = !Storage.SEND_CONSOLE_NOTIFICATION;
                            } else {
                                bool = Storage.NOTIFY_PLAYERS.contains(sender.getUniqueId());
                                if(bool) Storage.NOTIFY_PLAYERS.remove(sender.getUniqueId());
                                else Storage.NOTIFY_PLAYERS.add(sender.getUniqueId());
                            }

                            sender.sendMessage(bool ? Storage.ConfigSections.Messages.NOTIFICATION.DISABLED : Storage.ConfigSections.Messages.NOTIFICATION.ENABLED);
                            return;

                        case "ls":
                        case "list":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "list")) return;

                            stringList = StringUtils.buildStringList(
                                    Storage.Blacklist.getBlacklist().getCommands(),
                                    Storage.ConfigSections.Messages.BLACKLIST.LIST_SPLITTER,
                                    Storage.ConfigSections.Messages.BLACKLIST.LIST_COMMAND,
                                    "%command%"
                            );

                            sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.LIST_MESSAGE.replace("%size%", String.valueOf(Storage.Blacklist.getBlacklist().getCommands().size())).replace("%commands%", stringList));
                            return;

                        case "listgroups":
                        case "lg":

                            if(!PermissionUtil.hasPermissionWithResponse(sender, "listgroups")) return;

                            stringList = StringUtils.buildStringList(
                                    GroupManager.getGroupNames(),
                                    Storage.ConfigSections.Messages.GROUP.LIST_GROUP_SPLITTER,
                                    Storage.ConfigSections.Messages.GROUP.LIST_GROUP_GROUPS,
                                    "%group%");

                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.LIST_GROUP_MESSAGE.replace("%size%", String.valueOf(GroupManager.getGroupNames().size())).replace("%groups%", stringList));
                            return;

                        case "listpriorities":
                        case "lp":

                            if(!PermissionUtil.hasPermissionWithResponse(sender, "listpriorities")) return;

                            stringList = StringUtils.buildGroupStringList(
                                    GroupManager.getGroups(),
                                    Storage.ConfigSections.Messages.GROUP.LIST_PRIORITY_SPLITTER,
                                    Storage.ConfigSections.Messages.GROUP.LIST_PRIORITY_GROUPS);

                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.LIST_PRIORITY_MESSAGE.replace("%size%", String.valueOf(GroupManager.getGroups().size())).replace("%groups%", stringList));
                            return;

                        case "update":

                            if(!PermissionUtil.hasPermissionWithResponse(sender, "update_permissions")) return;

                            PermissionUtil.reloadPermissions();
                            sender.sendMessage(Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.UPDATE_ALL);
                            return;
                    }

                case 2:
                    task = args[0].toLowerCase();
                    sub = args[1];
                    bool = Storage.Blacklist.isListed(sub, false, false);
                    switch (task) {
                        case "ls":
                        case "list":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "list")) return;
                            group = GroupManager.getGroupByName(sub.toLowerCase());

                            if(group == null) {
                                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", sub));
                                return;
                            }

                            stringList = StringUtils.buildStringList(
                                    group.getCommands(),
                                    Storage.ConfigSections.Messages.GROUP.LIST_SPLITTER,
                                    Storage.ConfigSections.Messages.GROUP.LIST_COMMAND,
                                    "%command%");
                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.LIST_MESSAGE.replace("%size%", String.valueOf(group.getCommands().size())).replace("%commands%", stringList).replace("%group%", group.getGroupName()));
                            return;

                        case "listgroups":
                        case "lg":

                            if(!PermissionUtil.hasPermissionWithResponse(sender, "listgroups")) return;

                            if(!Reflection.isProxyServer()) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            stringList = StringUtils.buildStringList(
                                    GroupManager.getGroupNamesByServer(sub.toLowerCase()),
                                    Storage.ConfigSections.Messages.GROUP.LIST_GROUP_SERVER_SPLITTER,
                                    Storage.ConfigSections.Messages.GROUP.LIST_GROUP_SERVER_GROUPS,
                                    "%group%"
                            );

                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.LIST_GROUP_SERVER_MESSAGE.replace("%server%", sub.toLowerCase()).replace("%size%", String.valueOf(GroupManager.getGroupNamesByServer(sub.toLowerCase()).size())).replace("%groups%", stringList));
                            return;

                        case "creategroup":
                        case "cg":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "creategroup")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            if(sub.equalsIgnoreCase("commands")) return;
                            bool = GroupManager.isGroupRegistered(sub.toLowerCase());

                            if(!bool) GroupManager.registerGroup(sub.toLowerCase());
                            sender.sendMessage((bool ? Storage.ConfigSections.Messages.GROUP.ALREADY_EXIST : Storage.ConfigSections.Messages.GROUP.CREATE).replace("%group%", sub.toLowerCase()));
                            return;

                        case "deletegroup":
                        case "dg":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "deletegroup")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            bool = GroupManager.isGroupRegistered(sub.toLowerCase());
                            confirmationString = "deletegroup " + sub.toLowerCase();

                            if(CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                if (bool) GroupManager.unregisterGroup(sub.toLowerCase());
                                sender.sendMessage((bool ? Storage.ConfigSections.Messages.GROUP.DELETE : Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST).replace("%group%", sub.toLowerCase()));
                            } else {
                                CONFIRMATION.put(uuid, confirmationString);
                                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DELETE_CONFIRM);
                            }
                            return;

                        case "clear":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            group = GroupManager.getGroupByName(sub);

                            if(group == null) {
                                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", sub));
                                return;
                            }
                            confirmationString = "clear " + group.getGroupName();

                            if(CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                group.clear();
                                handleChange();

                                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.CLEAR.replace("%group%", group.getGroupName()));
                            } else {
                                CONFIRMATION.put(uuid, confirmationString);
                                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.CLEAR_CONFIRM);
                            }
                            return;

                        case "add":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            if (!bool) {
                                Storage.Blacklist.getBlacklist().add(sub).save();
                                handleChange();
                            }

                            sender.sendMessage((bool ? Storage.ConfigSections.Messages.BLACKLIST.ADD_FAILED : Storage.ConfigSections.Messages.BLACKLIST.ADD_SUCCESS).replace("%command%", sub));
                            return;

                        case "remove":
                        case "rem":
                        case "rm":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;

                            if(backend) {
                                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                return;
                            }

                            if (bool) {
                                Storage.Blacklist.getBlacklist().remove(sub).save();
                                handleChange();
                            }
                            sender.sendMessage((!bool ? Storage.ConfigSections.Messages.BLACKLIST.REMOVE_FAILED : Storage.ConfigSections.Messages.BLACKLIST.REMOVE_SUCCESS).replace("%command%", sub));
                            return;

                        case "update":
                            if(!PermissionUtil.hasPermissionWithResponse(sender, "update_permissions")) return;
                            uuid = !Reflection.isProxyServer() ? BukkitLoader.getUUIDByName(sub) : Reflection.isVelocityServer() ? VelocityLoader.getUUIDByName(sub) : BungeeLoader.getUUIDByName(sub);

                            if(uuid != null) {
                                PermissionUtil.reloadPermissions(uuid);
                                sender.sendMessage(Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.UPDATE_SPECIFIC.replace("%target%", sub));
                            } else sender.sendMessage(Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.PLAYER_NOT_ONLINE.replace("%target%", sub));
                            return;
                    }

                case 3:

                    if(args[0].equals("serv") || args[0].equals("server")) {

                        if(!Reflection.isProxyServer()) {
                            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                            return;
                        }

                        task = args[1].toLowerCase();
                        sub = args[2].toLowerCase();

                        switch (task) {
                            case "clear":

                                if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;

                                if (!Reflection.isProxyServer()) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                    return;
                                }

                                if(!BlacklistCreator.exist(sub)) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR_SERVER_NOT_FOUND.replace("%server%", sub));
                                    return;
                                }

                                confirmationString = "clear-server " + sub;

                                if (CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                    Storage.Blacklist.getBlacklist(sub).clear().save();
                                    handleChange(sub);
                                    sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR_SERVER.replace("%server%", sub));
                                } else {
                                    CONFIRMATION.put(uuid, confirmationString);
                                    sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR_SERVER_CONFIRM.replace("%server%", sub));
                                }
                                return;

                            case "ls": case "list":

                                if(!PermissionUtil.hasPermissionWithResponse(sender, "list")) return;

                                if (!Reflection.isProxyServer()) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                                    return;
                                }

                                if(!BlacklistCreator.exist(sub)) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.SERV_LIST.SERVER_DOES_NOT_EXIST.replace("%server%", sub));
                                    return;
                                }

                                stringList = StringUtils.buildStringList(
                                        Storage.Blacklist.getBlacklist(sub).getCommands(),
                                        Storage.ConfigSections.Messages.SERV_LIST.LIST_SERVER_SPLITTER,
                                        Storage.ConfigSections.Messages.SERV_LIST.LIST_SERVER_COMMAND,
                                        "%command%"
                                );

                                sender.sendMessage(Storage.ConfigSections.Messages.SERV_LIST.LIST_SERVER_MESSAGE.replace("%server%", sub.toLowerCase()).replace("%size%", String.valueOf(Storage.Blacklist.getBlacklist(sub).getCommands().size())).replace("%commands%", stringList));
                                return;

                        }
                    }

                    task = args[0].toLowerCase();
                    sub = args[1].toLowerCase();
                    extra = args[2].toLowerCase();
                    group = GroupManager.getGroupByName(sub);

                    if(task.equals("setpriority") || task.equals("sp")) {

                        if (!PermissionUtil.hasPermissionWithResponse(sender, "setpriority")) return;

                        if(backend) {
                            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                            return;
                        }

                        if(group == null) {
                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", extra));
                            return;
                        }
                        sub = group.getGroupName();

                        try {
                            int priority = Integer.parseInt(extra);
                            if(priority < 1) {
                                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.PRIORITY_FAILED.replace("%group%", sub).replace("%priority%", extra));
                                return;
                            }

                            group.setPriority(priority);
                            GroupManager.sort();
                            handleChange();

                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.PRIORITY_SUCCESS.replace("%group%", sub).replace("%priority%", extra));
                        } catch (Throwable throwable) {
                            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.PRIORITY_FAILED.replace("%group%", sub).replace("%priority%", extra));
                        }

                        return;
                    }

                    task = args[0].toLowerCase();
                    sub = args[1].toLowerCase();
                    extra = args[2].toLowerCase();
                    group = GroupManager.getGroupByName(extra);
                    if(group == null) {
                        sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", extra));
                        return;
                    }

                    extra = group.getGroupName();
                    bool = group.contains(sub, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, true, false);

                    switch (task) {

                        case "add":
                            if (!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;
                            if (!bool) {
                                group.add(sub);
                                handleChange();
                            }

                            sender.sendMessage((bool ? Storage.ConfigSections.Messages.GROUP.ADD_FAILED : Storage.ConfigSections.Messages.GROUP.ADD_SUCCESS).replace("%command%", sub).replace("%group%", extra));
                            return;

                        case "remove":
                        case "rem":
                        case "rm":
                            if (!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;
                            if (bool) {
                                group.remove(sub);
                                handleChange();
                            }
                            sender.sendMessage((!bool ? Storage.ConfigSections.Messages.GROUP.REMOVE_FAILED : Storage.ConfigSections.Messages.GROUP.REMOVE_SUCCESS).replace("%command%", sub).replace("%group%", extra));
                            return;
                    }

                case 4:
                    if(backend) {
                        sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                        return;
                    }

                    task = args[1].toLowerCase();
                    sub = args[2].toLowerCase();
                    extra = args[3].toLowerCase();

                    if(args[0].equals("serv") || args[0].equals("server")) {

                        if (!Reflection.isProxyServer()) {
                            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                            return;
                        }

                        switch (task) {
                            case "add":
                                if (!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;

                                bool = Storage.Blacklist.getBlacklist(sub).isListed(extra);
                                if (!bool) {
                                    Storage.Blacklist.getBlacklist(sub).add(extra).save();
                                    handleChange(sub);
                                }

                                sender.sendMessage((bool ? Storage.ConfigSections.Messages.BLACKLIST.ADD_SERVER_FAILED : Storage.ConfigSections.Messages.BLACKLIST.ADD_SERVER_SUCCESS).replace("%server%", sub).replace("%command%", extra));
                                return;

                            case "remove":
                            case "rem":
                            case "rm":
                                if (!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;

                                bool = !Storage.Blacklist.getBlacklist(sub).isListed(extra);
                                if (!bool) {
                                    Storage.Blacklist.getBlacklist(sub).remove(extra).save();
                                    handleChange(sub);
                                }

                                sender.sendMessage((bool ? Storage.ConfigSections.Messages.BLACKLIST.REMOVE_SERVER_FAILED : Storage.ConfigSections.Messages.BLACKLIST.REMOVE_SERVER_SUCCESS).replace("%server%", sub).replace("%command%", extra));
                                return;

                            case "clear":

                                if(!PermissionUtil.hasPermissionWithResponse(sender, "clear")) return;

                                confirmationString = "clear-server-group " + sub + " " + extra;

                                if (CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                                    for (Group groups : GroupManager.getGroupsByServer(sub))
                                        groups.clear(sub);

                                    handleChange(sub);
                                    sender.sendMessage(Storage.ConfigSections.Messages.GROUP.CLEAR_SERVER.replace("%group%", extra).replace("%server%", sub));
                                } else {
                                    CONFIRMATION.put(uuid, confirmationString);
                                    sender.sendMessage(Storage.ConfigSections.Messages.GROUP.CLEAR_SERVER_CONFIRM.replace("%group%", extra).replace("%server%", sub));
                                }
                                return;

                            case "ls": case "list":

                                if(!PermissionUtil.hasPermissionWithResponse(sender, "list")) return;

                                if(!BlacklistCreator.exist(sub)) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.SERV_LIST.SERVER_DOES_NOT_EXIST.replace("%group%", extra).replace("%server%", sub));
                                    return;
                                }

                                group = GroupManager.getGroupByName(extra);
                                if(!BlacklistCreator.exist(extra, sub) || group == null) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.SERV_LIST.GROUP_DOES_NOT_EXIST.replace("%group%", extra).replace("%server%", sub));
                                    return;
                                }

                                stringList = StringUtils.buildStringList(
                                        group.getCommands(sub),
                                        Storage.ConfigSections.Messages.SERV_LIST.LIST_GROUP_SPLITTER,
                                        Storage.ConfigSections.Messages.SERV_LIST.LIST_GROUP_COMMAND,
                                        "%command%"
                                );

                                sender.sendMessage(Storage.ConfigSections.Messages.SERV_LIST.LIST_GROUP_MESSAGE.replace("%group%", group.getGroupName()).replace("%server%", sub.toLowerCase()).replace("%size%", String.valueOf(group.getAllCommands(sub).size())).replace("%commands%", stringList));
                                return;
                        }
                    }

                case 5:
                    if(backend) {
                        sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                        return;
                    }

                    task = args[1].toLowerCase();
                    sub = args[2].toLowerCase();
                    extra = args[3].toLowerCase();
                    subExtra = args[4].toLowerCase();

                    if(args[0].equals("serv") || args[0].equals("server")) {

                        if(!Reflection.isProxyServer()) {
                            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                            return;
                        }

                        switch (task) {
                            case "add":
                                if (!PermissionUtil.hasPermissionWithResponse(sender, "add")) return;

                                group = GroupManager.getGroupByName(subExtra);
                                if(group == null) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", extra));
                                    return;
                                }

                                bool = group.containsOnServer(extra, sub, false);
                                if (!bool) {
                                    group.getOrCreateGroupBlacklist(sub, true);
                                    group.add(extra, sub);
                                    handleChange(sub);
                                }

                                sender.sendMessage((bool ? Storage.ConfigSections.Messages.GROUP.ADD_SERVER_FAILED : Storage.ConfigSections.Messages.GROUP.ADD_SERVER_SUCCESS).replace("%server%", sub).replace("%command%", extra).replace("%group%", subExtra));
                                return;

                            case "remove": case "rem": case "rm":
                                if (!PermissionUtil.hasPermissionWithResponse(sender, "remove")) return;

                                group = GroupManager.getGroupByName(subExtra);
                                if(group == null) {
                                    sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", extra));
                                    return;
                                }

                                bool = !group.containsOnServer(extra, sub, false);
                                if (!bool) {
                                    group.getOrCreateGroupBlacklist(sub, true);
                                    group.remove(extra, sub);
                                    handleChange(sub);
                                }

                                sender.sendMessage((bool ? Storage.ConfigSections.Messages.GROUP.REMOVE_SERVER_FAILED : Storage.ConfigSections.Messages.GROUP.REMOVE_SERVER_SUCCESS).replace("%server%", sub).replace("%command%", extra).replace("%group%", subExtra));
                                return;
                        }
                    }
                default:
                    for (String line : Storage.ConfigSections.Messages.HELP.MESSAGE.getLines()) sender.sendMessage(line.replace("&", "§").replace("%label%", label));
            }
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            sender.sendMessage(Storage.ConfigSections.Messages.COMMAND_FAILED.MESSAGE);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static List<String> handleTabComplete(Object senderObj, String[] args) {
        CommandSender sender = new CommandSender(senderObj);
        final List<String> suggestions = new ArrayList<>(), result = new ArrayList<>();
        final boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

        switch (args.length) {
            case 1:
                if(PermissionUtil.hasPermission(sender, "update_permissions"))
                    suggestions.add("update");
                if (!backend && PermissionUtil.hasPermission(sender, "stats") && Reflection.isProxyServer())
                    suggestions.add("stats");
                if (!backend && PermissionUtil.hasPermission(sender, "notify")) suggestions.add("notify");
                if (PermissionUtil.hasPermission(sender, "info")) suggestions.add("info");
                if (!backend && PermissionUtil.hasPermission(sender, "creategroup"))
                    suggestions.addAll(Arrays.asList("creategroup", "cg"));
                if (!backend && PermissionUtil.hasPermission(sender, "deletegroup"))
                    suggestions.addAll(Arrays.asList("deletegroup", "dg"));
                if (PermissionUtil.hasPermission(sender, "listgroups"))
                    suggestions.addAll(Arrays.asList("listgroups", "lg"));
                if (PermissionUtil.hasPermission(sender, "listpriorities"))
                    suggestions.addAll(Arrays.asList("listpriorities", "lp"));
                if (!backend && PermissionUtil.hasPermission(sender, "setpriority"))
                    suggestions.addAll(Arrays.asList("setpriority", "sp"));
                if (PermissionUtil.hasPermission(sender, "list"))
                    suggestions.addAll(Arrays.asList("list", "ls"));
                if (!backend && PermissionUtil.hasPermission(sender, "clear")) suggestions.add("clear");
                if (PermissionUtil.hasPermission(sender, "reload")) suggestions.addAll(Arrays.asList("reload", "rl"));
                if (Reflection.isProxyServer() && (PermissionUtil.hasPermission(sender, "add") || PermissionUtil.hasPermission(sender, "remove") || PermissionUtil.hasPermission(sender, "clear")))
                    suggestions.addAll(Arrays.asList("serv", "server"));
                if (!backend && PermissionUtil.hasPermission(sender, "add")) suggestions.add("add");
                if (!backend && PermissionUtil.hasPermission(sender, "remove"))
                    suggestions.addAll(Arrays.asList("remove", "rem", "rm"));
                break;
            case 2:
                if (!backend && Arrays.asList("deletegroup", "dg").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "deletegroup"))
                    suggestions.addAll(GroupManager.getGroupNames());
                if (!backend && args[0].equals("clear") && PermissionUtil.hasPermission(sender, "clear"))
                    suggestions.addAll(GroupManager.getGroupNames());
                if (args[0].toLowerCase().contains("update") && PermissionUtil.hasPermission(sender, "update_permissions"))
                    suggestions.addAll(!Reflection.isProxyServer() ? BukkitLoader.getPlayerNames() : Reflection.isVelocityServer() ? VelocityLoader.getPlayerNames() : BungeeLoader.getPlayerNames());
                if (Arrays.asList("ls", "list").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "list"))
                    suggestions.addAll(GroupManager.getGroupNames());
                if (Reflection.isProxyServer() && Arrays.asList("lg", "listgroups").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "listgroups"))
                    suggestions.addAll(Storage.Blacklist.getBlacklistServers());
                if (Reflection.isProxyServer() && Arrays.asList("serv", "server").contains(args[0].toLowerCase())) {
                    if (PermissionUtil.hasPermission(sender, "add")) suggestions.add("add");
                    if (PermissionUtil.hasPermission(sender, "remove"))
                        suggestions.addAll(Arrays.asList("rm", "rem", "remove"));
                    if (PermissionUtil.hasPermission(sender, "list"))
                        suggestions.addAll(Arrays.asList("ls", "list"));
                    if (PermissionUtil.hasPermission(sender, "clear")) suggestions.add("clear");
                }

                if (!backend && (args[0].equals("setpriority") || args[0].equals("sp")) && PermissionUtil.hasPermission(sender, "setpriority") && !Reflection.isProxyServer())
                    suggestions.addAll(GroupManager.getGroupNames());

                if (!backend && args[0].equals("add") && PermissionUtil.hasPermission(sender, "add") && !Reflection.isProxyServer())
                    suggestions.addAll(BukkitLoader.getNotBlockedCommands());
                if (!backend && Arrays.asList("remove", "rem", "rm").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove"))
                    suggestions.addAll(Storage.Blacklist.getBlacklist().getCommands());
                break;

            case 3:
                if (!backend && args[0].equals("add") && PermissionUtil.hasPermission(sender, "add"))
                    suggestions.addAll(GroupManager.getGroupNames());
                if (!backend && Arrays.asList("remove", "rem", "rm").contains(args[0].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove"))
                    suggestions.addAll(GroupManager.getGroupNamesByServer(args[2]));
                if (Reflection.isProxyServer() && Arrays.asList("serv", "server").contains(args[0].toLowerCase())) {
                    if (args[1].equals("add") && PermissionUtil.hasPermission(sender, "add"))
                        suggestions.addAll(Storage.getServers(true));
                    if (Arrays.asList("remove", "rem", "rm").contains(args[1].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove"))
                        suggestions.addAll(Storage.Blacklist.getBlacklistServers());
                    if (args[1].equals("clear") && PermissionUtil.hasPermission(sender, "clear"))
                        suggestions.addAll(Storage.Blacklist.getBlacklistServers());
                    if (Arrays.asList("list", "ls").contains(args[1].toLowerCase()) && PermissionUtil.hasPermission(sender, "list")) suggestions.addAll(Storage.Blacklist.getBlacklistServers());
                }
                break;

            case 4:
                if (Reflection.isProxyServer() && Arrays.asList("serv", "server").contains(args[0].toLowerCase())) {
                    if (Arrays.asList("remove", "rem", "rm").contains(args[1].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove")) {
                        suggestions.addAll(Storage.Blacklist.getBlacklist(args[2]).getCommands());
                    }

                    if (Arrays.asList("list", "ls").contains(args[1].toLowerCase()) && PermissionUtil.hasPermission(sender, "list"))
                        suggestions.addAll(GroupManager.getGroupNamesByServer(args[2]));

                    if (args[1].equals("clear") && PermissionUtil.hasPermission(sender, "clear"))
                        suggestions.addAll(GroupManager.getGroupNamesByServer(args[2]));
                }
                break;

            case 5:
                if (Reflection.isProxyServer() && Arrays.asList("serv", "server").contains(args[0].toLowerCase())) {
                    if (args[1].equals("add") && PermissionUtil.hasPermission(sender, "add")) suggestions.addAll(GroupManager.getGroupNames());
                    if (Arrays.asList("remove", "rem", "rm").contains(args[1].toLowerCase()) && PermissionUtil.hasPermission(sender, "remove")) suggestions.addAll(GroupManager.getGroupNamesByServer(args[2]));
                }
                break;
        }

        suggestions.stream().filter(suggestion -> suggestion.startsWith(args[args.length-1].toLowerCase())).forEach(result::add);
        return result;
    }

    private static void handleChange(String server) {
        Storage.Blacklist.clearServerBlacklists(server);
        GroupManager.clearServerGroupBlacklists();
        Communicator.syncData();

        if(Reflection.isVelocityServer()) VelocityAntiTabListener.updateCommands();
        else WaterfallAntiTabListener.updateCommands();

        Communicator.sendPermissionReset();
    }

    private static void handleChange() {
        if(Reflection.isProxyServer()) {

            GroupManager.clearServerGroupBlacklists();
            Communicator.syncData();

            if(Reflection.isVelocityServer()) VelocityAntiTabListener.updateCommands();
            else WaterfallAntiTabListener.updateCommands();

            Communicator.sendPermissionReset();
        } else {

            if (Reflection.getMinor() >= 16) {
                PermissionUtil.reloadPermissions();
                BukkitAntiTabListener.handleTabCompletion();
            }

        }
    }

    private static void handleReload() {
        if(!Reflection.isProxyServer()) {
            BackendUpdater.stop();
            BackendUpdater.start();
        }

        ConfigUpdater.broadcastMissingParts();
    }
}
