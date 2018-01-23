package ru.tstu.telegram.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class OptionOfTusk {
    InlineKeyboardMarkup keyboardMarkup;
    List<List<InlineKeyboardButton>> rowsInline;
    List<InlineKeyboardButton> rowInline;
    int Number;

    public OptionOfTusk(SendMessage response,int number){
        keyboardMarkup = new InlineKeyboardMarkup();
        rowsInline = new ArrayList<>();
        rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton()
                .setText("Follow task").setCallbackData("1"));
        rowInline.add(new InlineKeyboardButton()
                .setText("Solving task").setCallbackData("2"));
        rowsInline.add(rowInline);
        keyboardMarkup.setKeyboard(rowsInline);
        Number = number;
        response.setReplyMarkup(keyboardMarkup);
    }

    public void ExecuteCommand(SendMessage response, String text, Integer userId){
       if(text.equals("1"))
       {
           //TstuBot.Follow();
           response.setText("follow " + userId + "task");
       }else{
           response.setText("solve " + userId + "task");
       }
    }

}
