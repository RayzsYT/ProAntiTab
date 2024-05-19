package de.rayzs.pat.api.brand;

public interface ServerBrand {
    void preparePlayer(Object playerObj);
    void send(Object playerObj);
    void initializeTask();
}
