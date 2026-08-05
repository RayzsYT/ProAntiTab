package de.rayzs.pat.plugin.system.subargument.handler;

import de.rayzs.pat.plugin.system.subargument.SubArgument;

public abstract class SubArgumentHandler {

    private final SubArgument instance;

    public SubArgumentHandler(SubArgument instance) {
        this.instance = instance;
    }

    protected SubArgument getInstance() {
        return instance;
    }
}
