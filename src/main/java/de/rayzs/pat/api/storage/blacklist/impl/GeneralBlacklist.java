package de.rayzs.pat.api.storage.blacklist.impl;

import de.rayzs.pat.api.storage.StorageTemplate;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;

import java.util.List;

public class GeneralBlacklist extends BlacklistStorage {

    public GeneralBlacklist(String navigatePath) {
        super("global." + navigatePath);
    }

    @Override
    public List<String> getCommands() {
        return super.getCommands();
    }

    @Override
    public BlacklistStorage add(String command) {
        return super.add(command);
    }

    @Override
    public BlacklistStorage remove(String command) {
        return super.remove(command);
    }

    @Override
    public BlacklistStorage clear() {
        return super.clear();
    }

    @Override
    public StorageTemplate setIfEmpty(String target, Object obj) {
        return super.setIfEmpty(target, obj);
    }

    @Override
    public StorageTemplate set(String target, Object obj) {
        return super.set(target, obj);
    }

    @Override
    public void setList(List<String> commands) {
        super.setList(commands);
    }

    @Override
    public Object get(String target, Object obj) {
        return super.get(target, obj);
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void load() {
        super.load();
    }
}
