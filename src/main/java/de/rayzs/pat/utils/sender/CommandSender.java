package de.rayzs.pat.utils.sender;

import de.rayzs.pat.utils.group.Group;

import java.util.List;
import java.util.UUID;

public interface CommandSender {

    Object getSenderObject();

    boolean isConsole();
    boolean isPlayer();

    boolean isOperator();
    boolean hasPermission(String permission);

    UUID getUniqueId();

    String getName();
    String getServerName();

    List<Group> getGroups();
    void updateGroups();

    void sendMessage(String message);
}
