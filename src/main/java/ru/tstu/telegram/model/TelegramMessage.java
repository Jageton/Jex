package ru.tstu.telegram.model;

/**
 * Created by user on 15.11.17.
 */
public class TelegramMessage {
    private String userId;
    private String text;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
