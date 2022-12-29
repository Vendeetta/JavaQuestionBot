package com.example.JavaQuestionBot.service;

public interface CallBackQueryHandler {
    void callBackDataProcessing(String callBackData, int messageId, Long chatId);
}
