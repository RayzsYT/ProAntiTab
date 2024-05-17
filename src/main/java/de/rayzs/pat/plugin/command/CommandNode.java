package de.rayzs.pat.plugin.command;

public interface CommandNode {
    String name();
    default boolean equals(String target, boolean intensive) {
        target = target.toLowerCase();

        String[] split;
        if(target.contains(" ")) {
            split = target.split(" ");
            if(split.length > 0) target = split[0];
            target = target.split(" ")[0];
        }

        if(intensive && target.contains(":")) {
            split = target.split(":");
            if(split.length > 0)
                target = target.replaceFirst(split[0] + ":", "");
        }

        return target.equals(name());
    }
}
