package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.StorageTemplate;

import java.util.ArrayList;
import java.util.List;

public class IgnoredServersStorage extends StorageTemplate {

    private List<String> servers = new ArrayList<>();

    public IgnoredServersStorage(String navigatePath) {
        super(Storage.Files.STORAGE, navigatePath);
    }

    public IgnoredServersStorage add(String server) {
        server = server.toLowerCase();
        if(!servers.contains(server))
            servers.add(server);
        return this;
    }

    public IgnoredServersStorage remove(String server) {
        server = server.toLowerCase();
        servers.remove(server);
        return this;
    }

    public boolean isListed(String server) {
        return Storage.isServer(server, servers);
    }

    @Override
    public void save() { getConfig().save(); }

    @Override
    public void load() {
        getConfig().reload();
        servers = (ArrayList<String>) getConfig().getOrSet(getNavigatePath(), servers);
    }
}
