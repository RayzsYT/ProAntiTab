package de.rayzs.pat.utils.brand;

public interface ServerBrand {
    void preparePlayer(Object playerObj);
    void send(Object playerObj);
    void initializeTask();
}
