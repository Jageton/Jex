package ru.tstu.telegram.bot;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.api.methods.send.SendInvoice;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.tstu.telegram.dao.MessagesDAOService;
import ru.tstu.telegram.model.TelegramMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Stream;

/**
 * This example bot is an echo bot that just repeats the messages sent to him
 */
@Component
public class TstuBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TstuBot.class);
    private BotTree BotTreeMenu;
    private HashMap<String,CallbackQuery> CallbackQueries;
    public TstuBot() {
        CallbackQueries = new HashMap<>();
        BotTreeMenu = new BotTree();
        Node n = new Node(NODE.Status.PorterNode);
        KeyboardButton kbb = new KeyboardButton();
        kbb.setText("Start");
        n.setKeyboardButton(kbb);
        n.SetName("Start");
        int index = BotTreeMenu.Init(n);

        kbb = new KeyboardButton();
        n = new Node(NODE.Status.PorterNode);
        kbb.setText("Profile");
        n.setKeyboardButton(kbb);
        n.SetName("Profile");
        index = BotTreeMenu.AddNode(0, n);

        /*n = new Node(NODE.Status.FinalNode);
        kbb = new KeyboardButton().setText("Statistic");
        n.setKeyboardButton(kbb);
        n.SetIResponse((SendMessage response, Integer userId) -> response.setText("Statistic should be there"));
        n.SetName("Statistic");
        BotTreeMenu.AddNode(index, n);*/

        n = new Node(NODE.Status.PorterNode);
        kbb = new KeyboardButton().setText("My Tasks");
        n.setName("My Tasks");
        n.setKeyboardButton(kbb);
        index = BotTreeMenu.AddNode(index, n);

        n = new Node(NODE.Status.FinalNode);
        kbb = new KeyboardButton().setText("My solving tasks");
        n.setKeyboardButton(kbb);
        n.setName("My solving tasks");
        n.SetIResponse((SendMessage response, Integer userId) -> //response.setText("My solving tasks should be there"));
        {
            messagesDAOService.getFollowTasks(response, userId);
            return response;
        });
        BotTreeMenu.AddNode(index, n);

        /*n = new Node(NODE.Status.FinalNode);
        kbb = new KeyboardButton().setText("My added tasks");
        n.setKeyboardButton(kbb);
        n.setName("My added tasks");
        n.SetIResponse((SendMessage response, Integer userId) -> response.setText("My added tasks should be there"));
        BotTreeMenu.AddNode(index, n);*/

        n = new Node(NODE.Status.FinalNode);
        kbb = new KeyboardButton().setText("My current task");
        n.setKeyboardButton(kbb);
        n.setName("My current task");
        n.SetIResponse((SendMessage response, Integer userId) -> //response.setText("My current task should be there"));
        {
            messagesDAOService.getCurrentTask(response, userId);
            return response;
        });
        BotTreeMenu.AddNode(index, n);

        n = new Node(NODE.Status.PorterNode);
        kbb = new KeyboardButton().setText("All tasks");
        n.setName("All tasks");
        n.setKeyboardButton(kbb);
        index = BotTreeMenu.AddNode(0, n);


        n = new Node(NODE.Status.FinalNode);
        kbb = new KeyboardButton().setText("Get all tasks");
        n.setKeyboardButton(kbb);
        n.setName("Get all tasks");
        n.SetIResponse((SendMessage response, Integer userId) -> {
            messagesDAOService.getAllTasks(response);
            return response;
        });
        BotTreeMenu.AddNode(index, n);

        n = new Node(NODE.Status.FinalNode);
        kbb = new KeyboardButton().setText("Get Last Task");
        n.setKeyboardButton(kbb);
        n.setName("Get Last Task");
        n.SetIResponse((SendMessage response, Integer userId) -> {
            messagesDAOService.getLastTask(response);
            return response;
        });
        BotTreeMenu.AddNode(index, n);

        n = new Node(NODE.Status.DialogNode);
        kbb = new KeyboardButton().setText("Chose task");
        n.setKeyboardButton(kbb);
        n.setName("Chose task");
        n.setIresponseDialog((SendMessage response, String text, Integer userId, int number) -> {
            switch (number)
            {
                case 0:
                    response.setText("Enter number of task");
                    return true;
                case 1:
                   messagesDAOService.getChoseTask(response, text);
                   return false;
                   default: return false;
            }
        });
        BotTreeMenu.AddNode(index, n);
    }

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    /*public void Follow(SendMessage response, String text, Integer userId){

    }

    public void Solve(SendMessage response, String text, Integer userId){

    }*/

    @Autowired
    private MessagesDAOService messagesDAOService;

    public OptionOfTusk OptOfTusk;

    @Override
    public void onUpdateReceived(Update update) {
        String text;
        if(update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            if (!BotTreeMenu.ChatIsNew(chatId)) {
                BotTreeMenu.InitNewChat(chatId);
            }
            response.setChatId(chatId);
            response.enableMarkdown(true);
            response = BotTreeMenu.GetResponse(response, message.getText(), chatId, message.getFrom().getId());
            try {
                sendApiMethod(response);
            } catch (TelegramApiException e) {
                logger.error("Error: {}, cause: {}", e.getMessage(), e.getCause());
            }
        }else if(update.hasCallbackQuery()){
           CallbackQuery message = update.getCallbackQuery();
           SendMessage response = new SendMessage();
           Long chatId = message.getMessage().getChatId();
           response.setChatId(chatId);
           response.enableMarkdown(true);
            OptOfTusk.ExecuteCommand(response, message.getData(), message.getFrom().getId());
            try {
                sendApiMethod(response);
            } catch (TelegramApiException e) {
                logger.error("Error: {}, cause: {}", e.getMessage(), e.getCause());
            }
        }
    }

}
