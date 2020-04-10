package com.example.a2hands.chat;

public class Chat {
    private String MSGID,Message,MessageImage,Receiver,Sender,Timestamp;
    private boolean isSeen,isDeleted;

    public Chat() {

    }

    public Chat(String MSGID, String message, String messageImage, String receiver, String sender, String timestamp, boolean isSeen, boolean isDeleted) {
        this.MSGID = MSGID;
        Message = message;
        MessageImage = messageImage;
        Receiver = receiver;
        Sender = sender;
        Timestamp = timestamp;
        this.isSeen = isSeen;
        this.isDeleted = isDeleted;
    }

    public String getMSGID() {
        return MSGID;
    }

    public void setMSGID(String MSGID) {
        this.MSGID = MSGID;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getMessageImage() {
        return MessageImage;
    }

    public void setMessageImage(String messageImage) {
        MessageImage = messageImage;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        Receiver = receiver;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public boolean getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean seen) {
        isSeen = seen;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}