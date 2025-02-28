package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.api.storage.*;
import org.bukkit.entity.Player;

public abstract class PlaceholderStorage extends StorageTemplate {

    private final String request;

    public PlaceholderStorage(String request) {
        super(Storage.Files.PLACEHOLDERS, request);

        this.request = request;
        Storage.ConfigSections.PLACEHOLDERS.add(this);
    }

    @Override
    public void save() { getConfig().save(); }

    @Override
    public void load() { getConfig().reload(); }

    public String getRequest() {
        return request;
    }

    public abstract String onRequest(Player player, String param);
}
