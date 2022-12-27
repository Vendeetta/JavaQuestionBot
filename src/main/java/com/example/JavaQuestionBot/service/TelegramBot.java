package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.config.BotConfig;
import com.example.JavaQuestionBot.exceptions.UnknownQuestionException;
import com.example.JavaQuestionBot.model.Category;
import com.example.JavaQuestionBot.model.QuestionRepository;
import com.example.JavaQuestionBot.model.DBQuestionRow;
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
//    private DBQuestionRow currentQuestion;

//    private Category category;

//    private List<DBQuestionRow> currentCategoryQuestions;
    private Map<Long, UserChatData> personalDataFoUsers;
    private final static String UNKNOWN_COMMAND = "Извините, я не знаю такой команды.";
    private final static String HELP_TEXT = "Данный бот написан для помощи в изучении Java.\n\n" +
                                            "По команде /question бот выдает произвольный вопрос по Java.\n\n" +
                                            "После ответа на вопрос, Вы можете посмотреть ответ, предлагаемый ботом.\n\n"+
                                            "Продуктивного Вам обученияй! ;)";
    private final static String SHOW_ANSWER_BUTTON = "YES_BUTTON";
    private final static String JAVA_CORE_BUTTON = "JAVA_CORE_BUTTON";
    private final static String SQL_BUTTON = "SQL_BUTTON";

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
        UserChatData pers1 = null;
        UserChatData pers2 = null;
        Message message = update.getMessage();
        if(update.hasMessage() && !personalDataFoUsers.containsKey(message.getChatId())) {
            personalDataFoUsers.put(message.getChatId(), new UserChatData(message.getChatId()));
        }
        if (update.hasMessage() && message.hasText()) {
            System.out.println("здесь2");
            String messageText = message.getText();
            String userName = message.getChat().getFirstName();
            long chatId = message.getChatId();


            System.out.println(personalDataFoUsers.get(chatId));
            switch (messageText) {
                case "/start":
                case "/старт":
                    startCommandReceived(chatId, userName);
                    break;
                case "/help":
                    helpCommandReceived(chatId, userName);
                    break;
                case "/question":
                    if(personalDataFoUsers.get(chatId).getCurrentCategory() == null){
                        askCurrentCategory(chatId);
                        break;
                    }
                    DBQuestionRow question = getQuestion(personalDataFoUsers.get(chatId));
                    if(question == null) throw new UnknownQuestionException();
                    personalDataFoUsers.get(chatId).setCurrentDBRow(question);
                    Thread.sleep(500);
                    askQuestion(chatId, personalDataFoUsers.get(chatId));

                    break;
                case "/category":
                    if(personalDataFoUsers.get(chatId).getQuestions() != null) {
                        sendMessage(chatId, "В данным момент тема уже установлена: \n" + personalDataFoUsers.get(chatId).getCurrentCategory().getName());
                        break;
                    }
                    askCurrentCategory(chatId);
                    break;
                case "/currenttheme":
                    sendMessage(chatId, "Текущая тема вопросов: \n" + personalDataFoUsers.get(chatId).getCurrentCategory().getName());
                    break;
                default: sendMessage(chatId, UNKNOWN_COMMAND);
            }

        } else if (update.hasCallbackQuery()) {
            System.out.println("здесь1");
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
               long chatId = update.getCallbackQuery().getMessage().getChatId();
//            UserChatData personalData = personalDataFoUsers.get(message.getChatId());
            pers2 = personalDataFoUsers.get(chatId);
            String callBackData = update.getCallbackQuery().getData();

            if(callBackData.equals(SHOW_ANSWER_BUTTON)){
                EditMessageText editMessageText = new EditMessageText();
//                long chatId = update.getCallbackQuery().getMessage().getChatId();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(messageId);
                editMessageText.setText(personalDataFoUsers.get(chatId).getCurrentDBRow().getAnswer());
                try {
                    execute(editMessageText);
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
            }
            if(callBackData.equals(JAVA_CORE_BUTTON)){
//                long chatId = update.getCallbackQuery().getMessage().getChatId();
                personalDataFoUsers.get(chatId).setQuestions(questionRepository.findByCategory("java"));
                System.out.println(personalDataFoUsers.get(chatId).getQuestions());
                sendMessage(chatId, "Тема установлена: Java Core.");
                personalDataFoUsers.get(chatId).setCurrentCategory(Category.Java_Core);
            }
            if(callBackData.equals(SQL_BUTTON)){
                System.out.println("здесь");
//                long chatId = update.getCallbackQuery().getMessage().getChatId();
                personalDataFoUsers.get(chatId).setQuestions(questionRepository.findByCategory("sql"));
                sendMessage(chatId, "Тема установлена: SQL.");
                personalDataFoUsers.get(chatId).setCurrentCategory(Category.SQL);
            }
        }
    }

    private void askCurrentCategory(long chatId) {
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

        rowInline.add(javaCoreButton);
        rowInline.add(sqlButton);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void askQuestion(Long chatId, UserChatData personalData) {
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
    private DBQuestionRow getQuestion(UserChatData personalData){
        int random = new Random().nextInt(personalData.getQuestions().size());
//        Optional<DBQuestionRow> optionalQuestion = questionRepository.findById(random);
        DBQuestionRow question = personalData.getQuestions().get(random);
//        if(optionalQuestion.isPresent()){
//            question = optionalQuestion.get();
//        }
//        for(DBQuestionRow q : questionRepository.findByCategory("java")){
//            System.out.println(q.getQuestion());
//        }

        return question;
    }

}
