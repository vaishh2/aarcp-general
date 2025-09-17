package com.recipeai.recipebackend.controller;

import com.recipeai.recipebackend.service.DocumentAIService;
import com.recipeai.recipebackend.service.ExcelService;
import com.recipeai.recipebackend.util.WordToPdfConverter;
import com.recipeai.recipebackend.service.DocumentAIService.ExtractionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/document")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private DocumentAIService documentAIService;

    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            String contentType = file.getContentType();

            // Convert DOCX → PDF (if needed) before sending to Document AI
            if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)
                    || file.getOriginalFilename().toLowerCase().endsWith(".docx")) {
                fileBytes = WordToPdfConverter.convertDocxToPdf(fileBytes);
            }

            List<ExtractionResult> results = documentAIService.processDocument(fileBytes);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No invoice data found (entities/line items missing).".getBytes());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            excelService.writeToExcel(results, out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("document_output.xlsx").build());

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error processing file: " + e.getMessage()).getBytes());
        }
    }
}
