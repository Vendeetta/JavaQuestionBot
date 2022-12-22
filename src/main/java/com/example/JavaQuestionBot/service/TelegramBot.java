package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final static String UNKNOWN_COMMAND = "Извините, я не знаю такой команды.";
    private final static String HELP_TEXT = "Данный бот написан для помощи в изучении Java.\n\n" +
                                            "По команде /question бот выдает произвольный вопрос по Java.\n\n" +
                                            "После ответа на вопрос, Вы можете посмотреть ответ, предлагаемый ботом.\n\n"+
                                            "Продуктивного Вам обученияй! ;)";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Запустить бота."));
        commands.add(new BotCommand("/question", "Задать произвольный вопрос по Java"));
        commands.add(new BotCommand("/help", "Получить справку по боту"));
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при установке меню бота :" + e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            long chatId = message.getChatId();
            String userName = message.getChat().getFirstName();
            switch (messageText) {
                case "/start":
                case "/старт":
                    startCommandReceived(chatId, userName);
                    break;
                case "/help":
                    helpCommandReceived(chatId, userName);
                    break;
                default: sendMessage(chatId, UNKNOWN_COMMAND);
            }
        }
    }

    private void startCommandReceived(Long chatId, String userName) {
        String answer = "Привет, " + userName + "!";
        log.info("Replied to " + userName);
        sendMessage(chatId, answer);
    }

    private void helpCommandReceived(Long chatId, String userName) {
        log.info("HELP COMMAND: Replied to " + userName);
        sendMessage(chatId, HELP_TEXT);
    }

    private void sendMessage (long chatId, String textToSend){
        SendMessage messenger = new SendMessage();
        messenger.setChatId(String.valueOf(chatId));
        messenger.setText(textToSend);
        try {
            execute(messenger);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
