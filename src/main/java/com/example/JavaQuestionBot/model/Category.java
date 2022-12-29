package com.example.JavaQuestionBot.model;

public enum Category {
    Java_Core("Java Core"),
    SQL("SQL"),
    OOP("OOP");
    private String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
