package com.charu.cognitiveload.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class TextExtractionService {

    public String extract(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename().toLowerCase();

        if (filename.endsWith(".pdf")) {
            return extractFromPDF(file);
        } else if (filename.endsWith(".docx")) {
            return extractFromDOCX(file);
        }
        return "";
    }

    private String extractFromPDF(MultipartFile file) throws IOException {
        // PDFBox 3.x uses Loader.loadPDF() instead of PDDocument.load()
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text;
    }

    private String extractFromDOCX(MultipartFile file) throws IOException {
        XWPFDocument document = new XWPFDocument(file.getInputStream());
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        StringBuilder sb = new StringBuilder();
        for (XWPFParagraph para : paragraphs) {
            if (!para.getText().trim().isEmpty()) {
                sb.append(para.getText()).append("\n\n");
            }
        }
        document.close();
        return sb.toString();
    }
}