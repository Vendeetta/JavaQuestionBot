package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.config.BotConfig;
import com.example.JavaQuestionBot.model.QuestionRepository;
import com.example.JavaQuestionBot.model.UserChatData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private QuestionRepository questionRepository;
    private final BotConfig config;

    public Map<Long, UserChatData> getPersonalDataFoUsers() {
        return personalDataFoUsers;
    }

    private Map<Long, UserChatData> personalDataFoUsers;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Запустить бота."));
        commands.add(new BotCommand("/question", "Задать произвольный вопрос по Java."));
        commands.add(new BotCommand("/help", "Получить справку по боту."));
        commands.add(new BotCommand("/category", "Установить тему задаваемых вопросов."));
        commands.add(new BotCommand("/currenttheme", "Установленная в данным момент тема вопросов."));
        personalDataFoUsers = new HashMap<>();
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при установке меню бота :" + e.getMessage());
        }
    }

    public QuestionRepository getQuestionRepository() {
        return questionRepository;
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if(update.hasMessage() && !personalDataFoUsers.containsKey(message.getChatId())) {
            personalDataFoUsers.put(message.getChatId(), new UserChatData(message.getChatId()));
        }
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            String userName = message.getChat().getFirstName();
            long chatId = message.getChatId();
            MessageHandler messageHandler = new MessageHandlerImpl(this);
            messageHandler.messageProcessing(messageText, userName, chatId);

        } else if (update.hasCallbackQuery()) {
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callBackData = update.getCallbackQuery().getData();
            CallBackQueryHandler callBackQueryHandler = new CallBackQueryHandlerImpl(this);
            callBackQueryHandler.callBackDataProcessing(callBackData, messageId, chatId);
        }
    }

    void sendMessage (long chatId, String textToSend){
        SendMessage messenger = new SendMessage();
        messenger.setChatId(String.valueOf(chatId));
        messenger.setText(textToSend);
        executeCatcher(messenger);
    }
    void executeCatcher(SendMessage messenger){
        try {
            execute(messenger);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    void executeCatcher(EditMessageText messenger){
        try {
            execute(messenger);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

}
