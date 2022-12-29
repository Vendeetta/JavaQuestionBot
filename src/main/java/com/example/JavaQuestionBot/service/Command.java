package com.example.JavaQuestionBot.service;

import com.example.JavaQuestionBot.model.UserChatData;

public interface Command {
    void startCommandReceived(Long chatId, String userName);
    void helpCommandReceived(Long chatId, String userName);

    void askCurrentCategory(Long chatId);
    void askQuestion(Long chatId, UserChatData personalData);
}
