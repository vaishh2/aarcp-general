package com.recipeai.recipebackend.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;

public class WordToPdfConverter {

    public static byte[] convertDocxToPdf(byte[] docxBytes) throws IOException {
        // Open the Word document from byte array
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes));
             ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             PDDocument pdf = new PDDocument()) {

            // Loop through paragraphs in Word
            doc.getParagraphs().forEach(paragraph -> {
                try {
                    // Create a new page for each paragraph
                    PDPage page = new PDPage(PDRectangle.A4);
                    pdf.addPage(page);

                    PDPageContentStream content = new PDPageContentStream(pdf, page);
                    content.setFont(PDType1Font.HELVETICA, 12);
                    content.beginText();
                    content.newLineAtOffset(50, 750); // start position
                    content.showText(paragraph.getText());
                    content.endText();
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            pdf.save(outStream); // write PDF to memory
            return outStream.toByteArray();
        }
    }
}

