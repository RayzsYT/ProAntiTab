package de.rayzs.pat.api.communication;

public interface Client {
    String CHANNEL_NAME = "pat:channel";
    void send(Object packet);
}
