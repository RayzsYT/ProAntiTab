package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.storage.*;

import java.io.Serializable;
import java.util.*;

public class BlacklistStorage extends StorageTemplate implements Serializable {

    private List<String> commands = new ArrayList<>();
    private HashSet<String> hiddenCommands = new HashSet<>();

    // Precomputed O(1) membership caches derived from hiddenCommands. Rebuilt via
    // rebuildLookups() (atomic swap of fresh sets) so a concurrent reader never sees an
    // empty/partial set. transient: caches, not serialized, always rebuilt on load().
    // volatile: publish the new set to all threads on the hot path.
    private transient volatile Set<String> rawLookup = new HashSet<>(), firstArgLookup = new HashSet<>();
    private transient volatile boolean lookupCaseSensitive = false;
    // Dirty flag: defer the O(n) rebuild off add()/remove()/clear() so bulk imports stay
    // O(1) per mutation instead of O(n); the rebuild runs lazily on the next isListed().
    private transient volatile boolean lookupDirty = true;

    public BlacklistStorage(String navigatePath) {
        super(Storage.Files.STORAGE, navigatePath);
    }

    public boolean isListed(String command) {
        final boolean caseSensitive = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;
        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;

        if (commands == null || command.isEmpty()) {
            return false;
        }

        // Rebuild lazily if the case-sensitivity toggle changed at runtime or the cache is dirty.
        if (lookupDirty || caseSensitive != lookupCaseSensitive) {
            rebuildLookups();
            lookupDirty = false;
        }

        final boolean isNegated = Storage.Blacklist.BlockTypeFetcher.isNegated(command);
        final boolean takeFirstArgument = !isNegated == turn;

        String query = command;
        if (takeFirstArgument) {
            query = StringUtils.getFirstArg(query);
        }
        if (!caseSensitive) {
            query = query.toLowerCase(Locale.ROOT);
        }

        // takeFirstArgument path -> firstArgLookup (entries first-arg-normalized), else rawLookup.
        return (takeFirstArgument ? firstArgLookup : rawLookup).contains(query);
    }

    /** Precomputes O(1) membership sets (raw + first-arg-normalized) from hiddenCommands. */
    private void rebuildLookups() {
        synchronized (this) { // guard hiddenCommands iteration vs concurrent add/remove/clear
            final boolean cs = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;
            // Build into local sets first, then assign atomically: never publish a half-built set.
            Set<String> newRawLookup = new HashSet<>();
            Set<String> newFirstArgLookup = new HashSet<>();
            for (String hc : hiddenCommands) {
                if (hc == null)
                    continue;

                newRawLookup.add(cs ? hc : hc.toLowerCase(Locale.ROOT));
                String firstArg = StringUtils.getFirstArg(hc);
                if (firstArg == null)
                    continue;
                newFirstArgLookup.add(cs ? firstArg : firstArg.toLowerCase(Locale.ROOT));
            }
            this.rawLookup = newRawLookup;
            this.firstArgLookup = newFirstArgLookup;
            this.lookupCaseSensitive = cs;
        }
    }

    public void setList(List<String> commands) {
        this.commands = commands;
    }

    public BlacklistStorage add(String command) {
        synchronized (this) {
            if (!commands.contains(command)) {
                commands.add(command);
                hiddenCommands.add(command);
                markDirty();
            }
        }

        return this;
    }

    public BlacklistStorage remove(String command) {
        synchronized (this) {
            commands.remove(command);
            hiddenCommands.remove(command);
            markDirty();
        }

        return this;
    }

    private void markDirty() {
        this.lookupDirty = true;
    }

    public BlacklistStorage clear() {
        synchronized (this) {
            commands.clear();
            hiddenCommands.clear(); // lookup source must be cleared too, or cache stays stale
            markDirty();
        }

        return this;
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public void save() {
        getConfig().setAndSave(getNavigatePath(), commands);
    }

    @Override
    public void load() {
        getConfig().reload();

        final boolean caseSensitive = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;


        commands = (ArrayList<String>) getConfig().getOrSet(getNavigatePath(), commands);

        final List<String> tmpCommands = commands != null ? new ArrayList<>(commands) : new ArrayList<>();
        final HashSet<String> pluginListCommands = new HashSet<>(), negatedPluginListCommands = new HashSet<>();

        final String pluginCommandPrefix = "plugin=";
        final String negatedPluginCommandPrefix = Storage.Blacklist.BlockType.NEGATE + pluginCommandPrefix;

        for (String command : tmpCommands) {
            if (command.startsWith(pluginCommandPrefix)) {
                pluginListCommands.add(command);
            }

            if (command.startsWith(negatedPluginCommandPrefix)) {
                negatedPluginListCommands.add(command);
            }
        }

        tmpCommands.removeAll(pluginListCommands);
        tmpCommands.removeAll(negatedPluginListCommands);

        for (String negatedPluginCommand : negatedPluginListCommands) {
            negatedPluginCommand = negatedPluginCommand.substring(negatedPluginCommandPrefix.length());

            List<String> pluginCommands = Storage.getLoader().getPluginCommands(negatedPluginCommand, false);

            for (String pluginCommand : pluginCommands) {
                tmpCommands.remove(pluginCommand);

                pluginCommand = Storage.Blacklist.BlockType.NEGATE + pluginCommand;
                tmpCommands.add(caseSensitive ? pluginCommand : pluginCommand.toLowerCase());
            }
        }

        for (String pluginListCommand : pluginListCommands) {
            pluginListCommand = pluginListCommand.substring(pluginCommandPrefix.length());

            List<String> pluginCommands = Storage.getLoader().getPluginCommands(pluginListCommand, false);

            for (String pluginCommand : pluginCommands) {
                final String negated = Storage.Blacklist.BlockType.NEGATE + pluginCommand;

                if (!commands.contains(negated) && !tmpCommands.contains(negated)) {
                    tmpCommands.add(caseSensitive ? pluginCommand : pluginCommand.toLowerCase());
                }
            }
        }


        if (!Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            hiddenCommands = new HashSet<>(tmpCommands);
            rebuildLookups();
            return;
        }

        final List<String> finalisedCommands = new ArrayList<>();
        for (String command : tmpCommands) {

            if (Storage.Blacklist.BlockTypeFetcher.isNegated(command)) {
                finalisedCommands.add(caseSensitive ? command : StringUtils.lowercaseFirstArgument(command));
                continue;
            }

            if (command.contains(" ")) {
                final String firstArgument = StringUtils.getFirstArg(command);

                if (!finalisedCommands.contains(firstArgument)) {
                    finalisedCommands.add(caseSensitive ? firstArgument : firstArgument.toLowerCase());
                }
            }

            finalisedCommands.add(caseSensitive ? command : StringUtils.lowercaseFirstArgument(command));
        }

        hiddenCommands = new HashSet<>(finalisedCommands);
        rebuildLookups();
    }
}
