package de.rayzs.pat.utils.communication;

import de.rayzs.pat.utils.TimeConverter;

public class ClientInfo {

    private final String id;
    private String name;

    private boolean sentFeedback = false;
    private long syncTime = System.currentTimeMillis();

    public ClientInfo(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
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
}
