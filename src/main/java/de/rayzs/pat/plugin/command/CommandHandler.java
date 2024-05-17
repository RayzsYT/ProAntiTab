package de.rayzs.pat.plugin.command;

import de.rayzs.pat.utils.Storage;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    private String groupName = null;
    private final List<CommandNode> executes, tabs;

    /*
        Experimental for next update...
     */

    public CommandHandler() {
        this.executes = new ArrayList<>();
        this.tabs = new ArrayList<>();
    }

    public CommandHandler(String groupName) {
        this.groupName = groupName;
        this.executes = new ArrayList<>();
        this.tabs = new ArrayList<>();
    }
}
