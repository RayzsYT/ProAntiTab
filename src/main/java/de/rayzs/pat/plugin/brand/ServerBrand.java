package de.rayzs.pat.plugin.brand;

public interface ServerBrand {
    void preparePlayer(Object playerObj);
    void send(Object playerObj);
    void initializeTask();
}
