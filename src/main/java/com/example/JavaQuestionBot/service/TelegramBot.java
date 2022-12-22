package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.config.BotConfig;
import com.example.JavaQuestionBot.exceptions.UnknownQuestionException;
import com.example.JavaQuestionBot.model.QuestionRepository;
import com.example.JavaQuestionBot.model.DBQuestionRow;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private QuestionRepository questionRepository;
    private final BotConfig config;
    private DBQuestionRow currentQuestion;
    private final static String UNKNOWN_COMMAND = "Извините, я не знаю такой команды.";
    private final static String HELP_TEXT = "Данный бот написан для помощи в изучении Java.\n\n" +
                                            "По команде /question бот выдает произвольный вопрос по Java.\n\n" +
                                            "После ответа на вопрос, Вы можете посмотреть ответ, предлагаемый ботом.\n\n"+
                                            "Продуктивного Вам обученияй! ;)";
    private final static String SHOW_ANSWER_BUTTON = "YES_BUTTON";

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

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            String userName = message.getChat().getFirstName();
            long chatId = message.getChatId();
            switch (messageText) {
                case "/start":
                case "/старт":
                    startCommandReceived(chatId, userName);
                    break;
                case "/help":
                    helpCommandReceived(chatId, userName);
                    break;
                case "/question":
                    DBQuestionRow question = getQuestion();
                    if(question == null) throw new UnknownQuestionException();
                    currentQuestion = question;
                    Thread.sleep(500);
                    askQuestion(chatId);
                    break;
                default: sendMessage(chatId, UNKNOWN_COMMAND);
            }
        } else if (update.hasCallbackQuery()) {
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String callBackData = update.getCallbackQuery().getData();

            if(callBackData.equals(SHOW_ANSWER_BUTTON)){
                EditMessageText editMessageText = new EditMessageText();
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(messageId);
                editMessageText.setText(currentQuestion.getAnswer());
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
            }
        }
    }

    private void askQuestion(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(currentQuestion.getQuestion());

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

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
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
    private DBQuestionRow getQuestion(){
        long random = (long)new Random().nextInt(20);
        Optional<DBQuestionRow> optionalQuestion = questionRepository.findById(random);
        DBQuestionRow question = null;
        if(optionalQuestion.isPresent()){
            question = optionalQuestion.get();
        }
        return question;
    }

}
