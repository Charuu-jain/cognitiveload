package com.charu.cognitiveload.repository;

import com.charu.cognitiveload.model.ParagraphScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParagraphScoreRepository extends JpaRepository<ParagraphScore, Long> {
    List<ParagraphScore> findByDocumentId(Long documentId);
}