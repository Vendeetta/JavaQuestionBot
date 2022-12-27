package com.example.JavaQuestionBot.model;

import java.util.List;

public class UserChatData {

    private Long chatId;

    private List<DBQuestionRow> questions;
    private Category currentCategory;

    private DBQuestionRow currentDBRow;

    public UserChatData() {
    }

    public UserChatData(Long chatId) {
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public List<DBQuestionRow> getQuestions() {
        return questions;
    }

    public void setQuestions(List<DBQuestionRow> questions) {
        this.questions = questions;
    }

    public Category getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(Category currentCategory) {
        this.currentCategory = currentCategory;
    }

    public DBQuestionRow getCurrentDBRow() {
        return currentDBRow;
    }

    public void setCurrentDBRow(DBQuestionRow currentDBRow) {
        this.currentDBRow = currentDBRow;
    }
}


