package de.rayzs.pat.api.brand;

import de.rayzs.pat.utils.PacketUtils;

public interface ServerBrand {
    void preparePlayer(Object playerObj);
    void send(Object playerObj);
    PacketUtils.BrandManipulate createPacket(Object playerObj);
    void initializeTask();
}
