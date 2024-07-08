package de.rayzs.pat.api.event;

public abstract class PATEvent {

    public Situation situation;
    public abstract boolean shouldHandleByDefault(Object executor, String command);

    public enum Situation { EXECUTED_PLUGINS_COMMAND, EXECUTED_VERSION_COMMAND, EXECUTED_BLOCKED_COMMAND }
}
