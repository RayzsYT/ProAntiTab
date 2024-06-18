package de.rayzs.pat.api.storage.blacklist.impl;

import de.rayzs.pat.api.storage.storages.IgnoredServersStorage;

public class GeneralIgnoredServers extends IgnoredServersStorage {

    public GeneralIgnoredServers() {
        super("global.ignored-servers");
    }

    @Override
    public IgnoredServersStorage add(String server) {
        return super.add(server);
    }

    @Override
    public IgnoredServersStorage remove(String server) {
        return super.remove(server);
    }

    @Override
    public boolean isListed(String server) {
        return super.isListed(server);
    }

    @Override
    public boolean isListEmpty() {
        return super.isListEmpty();
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void save() {
        super.save();
    }
}
