package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
@Slf4j

public class CallBackQueryHandlerImpl implements CallBackQueryHandler {
    private TelegramBot bot;
    private final static String JAVA_CORE_BUTTON = "JAVA_CORE_BUTTON";
    private final static String SQL_BUTTON = "SQL_BUTTON";
    private final static String OOP_BUTTON = "OOP_BUTTON";
    private final static String SHOW_ANSWER_BUTTON = "YES_BUTTON";

    public CallBackQueryHandlerImpl(TelegramBot bot) {
        this.bot = bot;
    }
        public void callBackDataProcessing(String callBackData, int messageId, Long chatId) {
            if (callBackData.equals(SHOW_ANSWER_BUTTON)) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(messageId);
                editMessageText.setText(bot.getPersonalDataFoUsers().get(chatId).getCurrentDBRow().getAnswer());
                bot.executeCatcher(editMessageText);
            }
            if (callBackData.equals(JAVA_CORE_BUTTON)) {
                bot.getPersonalDataFoUsers().get(chatId).setQuestions(bot.getQuestionRepository().findByCategory("java"));
                bot.sendMessage(chatId, "Тема установлена: Java Core.");
                bot.getPersonalDataFoUsers().get(chatId).setCurrentCategory(Category.Java_Core);
            }
            if (callBackData.equals(SQL_BUTTON)) {
                bot.getPersonalDataFoUsers().get(chatId).setQuestions(bot.getQuestionRepository().findByCategory("sql"));
                bot.sendMessage(chatId, "Тема установлена: SQL.");
                bot.getPersonalDataFoUsers().get(chatId).setCurrentCategory(Category.SQL);
            }
            if (callBackData.equals(OOP_BUTTON)) {
                bot.getPersonalDataFoUsers().get(chatId).setQuestions(bot.getQuestionRepository().findByCategory("oop"));
                bot.sendMessage(chatId, "Тема установлена: OOP.");
                bot.getPersonalDataFoUsers().get(chatId).setCurrentCategory(Category.OOP);
            }
        }
}
