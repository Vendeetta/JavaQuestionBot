package com.example.JavaQuestionBot.service;

public interface MessageHandler
{
    void messageProcessing(String message, String userName, Long charId);

}
