package com.theoflu.Document.Management.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileTextSearcher{

    public List<String> searchInFolder(String folderPath, String searchTerm) {
        List<String> founded= new ArrayList<>();
        StringBuilder result = new StringBuilder();
        try {
            Files.walk(Paths.get(folderPath)).filter(Files::isRegularFile).forEach(file -> {
                try {
                    String filePath = file.toString();
                    boolean found = false;
                    if (filePath.endsWith(".txt")) {
                        found = searchInTxt(filePath, searchTerm);
                    } else if (filePath.endsWith(".pdf")) {
                        found = searchInPdf(filePath, searchTerm);
                    } else if (filePath.endsWith(".docx")) {
                        found = searchInWord(filePath, searchTerm);
                    } else if (filePath.endsWith(".xlsx")) {
                        found = searchInExcel(filePath, searchTerm);
                    }
                    if (found) {
                        founded.add(filePath.substring(8));
                       // result.append("The term \"").append(searchTerm).append("\" is found in the file: ").append(filePath).append("\n");
                    } else {
                        result.append("The term \"").append(searchTerm).append("\" is not found in the file: ").append(filePath).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return founded;
    }

    private boolean searchInTxt(String filePath, String searchTerm) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(searchTerm)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchInPdf(String filePath, String searchTerm) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            return text.contains(searchTerm);
        }
    }

    private boolean searchInWord(String filePath, String searchTerm) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text.contains(searchTerm)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchInExcel(String filePath, String searchTerm) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(searchTerm)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
