package ru.tstu.telegram.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;

public class Dialog {
    Node DialogNode;
    int Number;

    public Dialog(Node dialogNode){
        DialogNode = dialogNode;
        Number = 0;
    }

    public boolean EnterDialog(SendMessage response, String text, Integer userId)
    {
        return DialogNode.GetResponseDialog(response, text, userId, Number++);
    }
}
