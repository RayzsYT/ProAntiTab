package de.rayzs.pat.utils.sender;

public abstract class CommandSenderAbstract implements CommandSender {

    private Object senderObj;

    public CommandSenderAbstract(Object senderObj) {
        this.senderObj = senderObj;
    }

    @Override
    public void updateSenderObject(Object senderObj) {
        this.senderObj = senderObj;
    }

    @Override
    public Object getSenderObject() {
        return senderObj;
    }
}
