package de.rayzs.pat.utils.sender;

import java.util.UUID;

public interface CommandSender {

    void updateSenderObject(Object senderObj);
    Object getSenderObject();

    boolean isConsole();
    boolean isPlayer();

    boolean isOperator();
    boolean hasPermission(String permission);

    UUID getUniqueId();

    String getName();
    String getServerName();

    void sendMessage(String message);
}
