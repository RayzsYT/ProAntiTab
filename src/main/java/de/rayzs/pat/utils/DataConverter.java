package de.rayzs.pat.utils;

import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;

public class DataConverter {

    public static String convertCommandsToString(List<String> commands) {
        if(commands.isEmpty()) return "§";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < commands.size(); i++) {
            result.append(commands.get(i));
            if(i < (commands.size() - 1)) result.append(";");
        }

        return result.toString();
    }

    public static String convertGroupsToString() {
        if(GroupManager.getGroups().isEmpty()) return "§";
        StringBuilder result = new StringBuilder();

        for (Group group : GroupManager.getGroups()) {
            result.append(group.getGroupName());
            for (String command : group.getCommands())
                result.append(";" + command);
            result.append("\\\\");
        }

        return result.toString();
    }
}
