package com.recipeai.recipebackend.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.*;

@Service
public class ExcelService {

    public void writeToExcel(List<DocumentAIService.ExtractionResult> results, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();

        // Always one sheet
        Sheet sheet = workbook.createSheet("Invoices");

        // Collect all unique headers (fixed + dynamic)
        Set<String> allHeadersSet = new LinkedHashSet<>();
        allHeadersSet.add("Customer Name");
        allHeadersSet.add("Customer Number");
        allHeadersSet.add("Invoice Number");
        allHeadersSet.add("Invoice Date");
        allHeadersSet.add("Invoice Amount");
        allHeadersSet.add("Line No");
        allHeadersSet.add("Quantity");
        allHeadersSet.add("Service Description");
        allHeadersSet.add("Line Amount");
        allHeadersSet.add("Total");

        for (DocumentAIService.ExtractionResult data : results) {
            allHeadersSet.addAll(data.headers);
        }
        List<String> allHeaders = new ArrayList<>(allHeadersSet);

        // Create header row
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        headerStyle.setFont(bold);

        for (int c = 0; c < allHeaders.size(); c++) {
            Cell cell = headerRow.createCell(c);
            cell.setCellValue(allHeaders.get(c));
            cell.setCellStyle(headerStyle);
        }

        // Write data rows (all invoices combined)
        int rowIdx = 1;
        for (DocumentAIService.ExtractionResult data : results) {
            for (Map<String, String> rowMap : data.rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int c = 0; c < allHeaders.size(); c++) {
                    String h = allHeaders.get(c);
                    row.createCell(c).setCellValue(rowMap.getOrDefault(h, ""));
                }
            }
        }

        // Grand total at bottom
        int lineAmountCol = allHeaders.indexOf("Line Amount");
        if (lineAmountCol != -1 && rowIdx > 1) {
            Row totalRow = sheet.createRow(rowIdx);
            int labelCol = Math.max(0, lineAmountCol - 1);
            totalRow.createCell(labelCol).setCellValue("Grand Total");
            String colLetter = CellReference.convertNumToColString(lineAmountCol);
            String formula = String.format("SUM(%s2:%s%d)", colLetter, colLetter, rowIdx);
            totalRow.createCell(lineAmountCol).setCellFormula(formula);
        }

        // Auto-size columns
        for (int c = 0; c < allHeaders.size(); c++) {
            sheet.autoSizeColumn(c);
        }

        workbook.write(outputStream);
        workbook.close();
    }
}
