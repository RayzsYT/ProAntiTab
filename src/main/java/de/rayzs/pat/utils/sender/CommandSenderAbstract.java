package de.rayzs.pat.utils.sender;

import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class CommandSenderAbstract implements CommandSender {

    private static final byte KEY = (byte) 0;

    private final Object senderObj;
    private final ExpireCache<Byte, List<Group>> groups = new ExpireCache<>(1, TimeUnit.HOURS);

    public CommandSenderAbstract(Object senderObj) {
        this.senderObj = senderObj;
    }

    @Override
    public Object getSenderObject() {
        return senderObj;
    }

    @Override
    public List<Group> getGroups() {
        final List<Group> groupList = groups.get(KEY);

        if (groupList == null) {
            return groups.putAndGet(KEY, GroupManager.getPlayerGroups(getUniqueId()));
        }

        return groupList;
    }

    protected void setGroups(List<Group> groups) {
        this.groups.putIgnoreIfContains(KEY, groups);
    }
}
