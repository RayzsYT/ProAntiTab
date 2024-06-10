package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.StorageTemplate;

public abstract class PlaceholderStorage extends StorageTemplate {

    private String request;

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

    public abstract String onRequest(String param);
}
