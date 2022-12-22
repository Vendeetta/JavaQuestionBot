package com.example.JavaQuestionBot.model;

import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<DBQuestionRow, Long> {
}
