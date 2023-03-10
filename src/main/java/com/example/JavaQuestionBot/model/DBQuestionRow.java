package com.example.JavaQuestionBot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "questionsDataTable")
public class DBQuestionRow {
    @Id
    private Long numberOfQuestion;

    private String question;

    private String answer;

    private String category;

    public Long getNumberOfQuestion() {
        return numberOfQuestion;
    }

    public void setNumberOfQuestion(Long numberOfQuestion) {
        this.numberOfQuestion = numberOfQuestion;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
