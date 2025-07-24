package de.rayzs.pat.api.command;

import de.rayzs.pat.utils.*;
import java.util.*;

public abstract class ProCommand {

    private final String name;
    private final List<String> aliases;

    protected boolean proxyOnly = false, serverCommand  = false;

    public ProCommand(String name, String... aliases) {
        this.name = name.toLowerCase();
        this.aliases = aliases != null && !aliases[0].isBlank() ? Arrays.asList(aliases) : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean isProxyOnly() {
        return proxyOnly;
    }

    public boolean isServerCommand() {
        return serverCommand;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isCommand(String label) {
        String commandName = StringUtils.getFirstArg(label);

        if (name.equalsIgnoreCase(commandName))
            return true;

        return !aliases.isEmpty() && aliases.stream().anyMatch(commandName::equalsIgnoreCase);
    }

    public abstract boolean execute(CommandSender sender, String[] args);
    public abstract List<String> tabComplete(CommandSender sender, String[] args);
}
