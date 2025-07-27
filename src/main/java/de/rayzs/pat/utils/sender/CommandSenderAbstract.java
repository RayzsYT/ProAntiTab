package de.rayzs.pat.utils.sender;

import java.util.UUID;

public abstract class CommandSenderAbstract implements CommandSender {

    private final Object senderObj;

    public CommandSenderAbstract(Object senderObj) {
        this.senderObj = senderObj;
    }

    @Override
    public Object getSenderObject() {
        return senderObj;
    }
}
