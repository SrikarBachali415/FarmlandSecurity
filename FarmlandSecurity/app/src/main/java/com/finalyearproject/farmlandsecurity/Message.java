package com.finalyearproject.farmlandsecurity;

public class Message {
    private final int id;
    private final String message;
    private final String timestamp;

    public Message(int id, String message, String timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
