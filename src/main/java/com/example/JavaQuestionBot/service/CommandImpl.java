package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.model.Category;
import com.example.JavaQuestionBot.model.UserChatData;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandImpl implements Command
{
    private TelegramBot bot;
    private final static String HELP_TEXT = "Данный бот написан для помощи в изучении Java.\n\n" +
                                            "По команде /question бот выдает произвольный вопрос по Java.\n\n" +
                                            "После ответа на вопрос, Вы можете посмотреть ответ, предлагаемый ботом.\n\n"+
                                            "Продуктивного Вам обученияй! ;)";
    private final static String JAVA_CORE_BUTTON = "JAVA_CORE_BUTTON";
    private final static String SQL_BUTTON = "SQL_BUTTON";
    private final static String OOP_BUTTON = "OOP_BUTTON";
    private final static String SHOW_ANSWER_BUTTON = "YES_BUTTON";

    public CommandImpl(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void startCommandReceived(Long chatId, String userName) {
        String answer = "Привет, " + userName + "!";
        log.info("Replied to " + userName);
        bot.sendMessage(chatId, answer);
    }

    @Override
    public void helpCommandReceived(Long chatId, String userName) {
        log.info("HELP COMMAND: Replied to " + userName);
        bot.sendMessage(chatId, HELP_TEXT);
    }

    @Override
    public void askCurrentCategory(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите тему:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var javaCoreButton = new InlineKeyboardButton();
        javaCoreButton.setText(Category.Java_Core.getName());
        javaCoreButton.setCallbackData(JAVA_CORE_BUTTON);

        var sqlButton = new InlineKeyboardButton();
        sqlButton.setText(Category.SQL.getName());
        sqlButton.setCallbackData(SQL_BUTTON);

        var oopButton = new InlineKeyboardButton();
        oopButton.setText(Category.OOP.getName());
        oopButton.setCallbackData(OOP_BUTTON);

        rowInline.add(javaCoreButton);
        rowInline.add(sqlButton);
        rowInline.add(oopButton);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        message.setReplyMarkup(markup);

        bot.executeCatcher(message);
    }

    @Override
    public void askQuestion(Long chatId, UserChatData personalData) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(personalData.getCurrentDBRow().getQuestion());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var showAnswerButton = new InlineKeyboardButton();
        showAnswerButton.setText("Показать ответ");
        showAnswerButton.setCallbackData(SHOW_ANSWER_BUTTON);

        rowInline.add(showAnswerButton);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        message.setReplyMarkup(markup);

        bot.executeCatcher(message);
    }
}
