package com.charu.cognitiveload.model;

import jakarta.persistence.*;

@Entity
@Table(name = "paragraph_scores")
public class ParagraphScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    @Column(columnDefinition = "TEXT")
    private String paragraphText;

    private Double cliScore;
    private String difficultyLevel;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getParagraphText() { return paragraphText; }
    public void setParagraphText(String paragraphText) { this.paragraphText = paragraphText; }

    public Double getCliScore() { return cliScore; }
    public void setCliScore(Double cliScore) { this.cliScore = cliScore; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
}