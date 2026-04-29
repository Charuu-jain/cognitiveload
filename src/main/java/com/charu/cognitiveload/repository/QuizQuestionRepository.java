package com.charu.cognitiveload.repository;

import com.charu.cognitiveload.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizQuestionRepository
        extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByDocumentId(Long documentId);
}