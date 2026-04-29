package com.charu.cognitiveload.controller;

import com.charu.cognitiveload.model.*;
import com.charu.cognitiveload.repository.*;
import com.charu.cognitiveload.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class DocumentController {

    @Autowired TextExtractionService extractionService;
    @Autowired CLIService cliService;
    @Autowired SummarizationService summarizationService;
    @Autowired GrammarCheckService grammarCheckService;
    @Autowired StudyMaterialService studyMaterialService;
    @Autowired QuizService quizService;
    @Autowired
    FlashCardService flashcardService;
    @Autowired DocumentRepository documentRepository;
    @Autowired ParagraphScoreRepository paragraphScoreRepository;
    @Autowired QuizQuestionRepository quizQuestionRepository;
    @Autowired FlashCardRepository flashcardRepository;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        if (session.getAttribute("user") == null)
            return "redirect:/login";
        return "index";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            Model model,
            HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        try {

            String text = extractionService.extract(file);


            Document doc = new Document();
            doc.setFileName(file.getOriginalFilename());
            doc.setFileType(file.getOriginalFilename()
                    .substring(file.getOriginalFilename()
                            .lastIndexOf('.') + 1));
            doc.setUploadTime(LocalDateTime.now());
            doc.setFullText(text);


            String summary = summarizationService
                    .summarize(text, 5);
            doc.setSummary(summary);
            documentRepository.save(doc);


            List<ParagraphScore> scores =
                    cliService.analyze(text, doc.getId());
            paragraphScoreRepository.saveAll(scores);


            double overallCLI = scores.stream()
                    .mapToDouble(ParagraphScore::getCliScore)
                    .average().orElse(0.0);
            overallCLI = Math.round(overallCLI * 10.0) / 10.0;

            String overallLevel = overallCLI >= 70 ? "HIGH"
                    : overallCLI >= 40 ? "MEDIUM" : "LOW";


            List<Map<String, String>> grammarErrors =
                    grammarCheckService.checkGrammar(
                            text.substring(0, Math.min(
                                    text.length(), 1500)));


            List<Map<String, String>> studyMaterials =
                    studyMaterialService
                            .findStudyMaterial(text);


            List<QuizQuestion> quizQuestions =
                    quizService.generateQuiz(text, doc.getId());
            quizQuestionRepository.saveAll(quizQuestions);


            List<FlashCard> flashcards =
                    flashcardService.generateFlashcards(
                            text, doc.getId());
            flashcardRepository.saveAll(flashcards);


            model.addAttribute("fileName",
                    file.getOriginalFilename());
            model.addAttribute("scores", scores);
            model.addAttribute("summary", summary);
            model.addAttribute("overallCLI", overallCLI);
            model.addAttribute("overallLevel", overallLevel);
            model.addAttribute("grammarErrors", grammarErrors);
            model.addAttribute("studyMaterials", studyMaterials);
            model.addAttribute("quizQuestions", quizQuestions);
            model.addAttribute("flashcards", flashcards);

            return "dashboard";

        } catch (Exception e) {
            model.addAttribute("error",
                    "Error: " + e.getMessage());
            return "index";
        }
    }
}