package de.rayzs.pat.utils.communication;

public interface Client {
    String CHANNEL_NAME = "pat:channel";
    void sendInformation(String information);
}
