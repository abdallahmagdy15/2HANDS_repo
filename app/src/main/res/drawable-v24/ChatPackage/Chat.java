package com.example.a2hands.ChatPackage;

public class Chat {
    private String Message,Receiver,Sender,Timestamp;
    boolean isSeen;

    public Chat() {

    }

    public Chat(String message, String receiver, String sender, String timestamp, boolean isSeen) {
        this.Message = message;
        this.Receiver = receiver;
        this.Sender = sender;
        this.Timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        this.Receiver = receiver;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        this.Sender = sender;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.Timestamp = timestamp;
    }

    public boolean getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean seen) {
        isSeen = seen;
    }
}