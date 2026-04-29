package com.charu.cognitiveload.model;

import jakarta.persistence.*;

@Entity
@Table(name = "flashcards")
public class FlashCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    @Column(length = 500)
    private String term;

    @Column(columnDefinition = "TEXT")
    private String definition;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
}