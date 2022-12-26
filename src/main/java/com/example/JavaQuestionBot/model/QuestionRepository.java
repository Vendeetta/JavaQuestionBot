package com.example.JavaQuestionBot.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuestionRepository extends CrudRepository<DBQuestionRow, Long> {
    List<DBQuestionRow> findByCategory(String category);
}
