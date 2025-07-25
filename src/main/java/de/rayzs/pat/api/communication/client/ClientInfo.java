package de.rayzs.pat.api.communication.client;

import de.rayzs.pat.utils.TimeConverter;

public abstract class ClientInfo {

    private String id, name;

    private boolean sentFeedback = false;
    private long syncTime = System.currentTimeMillis();

    public ClientInfo(String serverId) {
        this.id = serverId;
    }

    public ClientInfo(String serverId, String name) {
        this.id = serverId;
        this.name = name;
    }

    public abstract void sendBytes(byte[] bytes);

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String serverId) {
        this.id = serverId;
    }

    public String getName() {
        return name;
    }

    public boolean compareId(String id) {
        return this.id.equals(id);
    }

    public void syncTime() {
        syncTime = System.currentTimeMillis();
    }

    public String getSyncTime() {
        return TimeConverter.calcAndGetTime(syncTime);
    }

    public void setFeedback(boolean state) {
        this.sentFeedback = state;
    }

    public boolean hasSentFeedback() {
        return sentFeedback;
    }

    public String getId() {
        return id;
    }
}
