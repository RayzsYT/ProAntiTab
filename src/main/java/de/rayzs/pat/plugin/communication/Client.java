package de.rayzs.pat.plugin.communication;

public interface Client {
    String CHANNEL_NAME = "pat:channel";
    void sendInformation(String information);
}
