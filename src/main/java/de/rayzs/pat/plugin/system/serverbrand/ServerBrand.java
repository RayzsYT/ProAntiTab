package de.rayzs.pat.plugin.system.serverbrand;

import de.rayzs.pat.utils.PacketUtils;

public interface ServerBrand {

    void initializeTask();

    void preparePlayer(Object playerObj);
    void send(Object playerObj);

    PacketUtils.BrandManipulate createPacket(Object playerObj);

}
