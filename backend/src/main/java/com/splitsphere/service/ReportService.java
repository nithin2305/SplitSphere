package com.splitsphere.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.splitsphere.dto.TransactionResponse;
import com.splitsphere.model.Group;
import com.splitsphere.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final TransactionService transactionService;
    private final GroupRepository groupRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public byte[] generateExcelReport(Long groupId) throws IOException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        List<TransactionResponse> transactions = transactionService.getGroupTransactions(groupId);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Transaction Report: " + group.getName());
            titleCell.setCellStyle(headerStyle);
            
            // Header row
            Row headerRow = sheet.createRow(2);
            String[] columns = {"Date", "Type", "Description", "Amount", "Payer", "Details"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            int rowNum = 3;
            for (TransactionResponse transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getCreatedAt().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(transaction.getType());
                row.createCell(2).setCellValue(transaction.getDescription());
                row.createCell(3).setCellValue(transaction.getAmount().doubleValue());
                row.createCell(4).setCellValue(transaction.getPayerName());
                
                String details = "";
                if ("EXPENSE".equals(transaction.getType())) {
                    details = "Split: " + transaction.getParticipantNames() + " ($" + transaction.getPerPersonAmount() + " each)";
                } else if ("SETTLEMENT".equals(transaction.getType())) {
                    details = "Paid to: " + transaction.getPayeeName();
                    if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
                        details += " - " + transaction.getNote();
                    }
                }
                row.createCell(5).setCellValue(details);
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    public byte[] generatePdfReport(Long groupId) throws IOException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        List<TransactionResponse> transactions = transactionService.getGroupTransactions(groupId);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            document.add(new Paragraph("Transaction Report")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Group: " + group.getName())
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));
            
            // Table
            float[] columnWidths = {15, 10, 25, 10, 15, 25};
            Table table = new Table(columnWidths);
            table.setWidth(550);
            
            // Header
            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Payer").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Details").setBold()));
            
            // Data
            for (TransactionResponse transaction : transactions) {
                table.addCell(new Cell().add(new Paragraph(transaction.getCreatedAt().format(DATE_FORMATTER))));
                table.addCell(new Cell().add(new Paragraph(transaction.getType())));
                table.addCell(new Cell().add(new Paragraph(transaction.getDescription())));
                table.addCell(new Cell().add(new Paragraph("$" + transaction.getAmount())));
                table.addCell(new Cell().add(new Paragraph(transaction.getPayerName())));
                
                String details = "";
                if ("EXPENSE".equals(transaction.getType())) {
                    details = "Split: " + transaction.getParticipantNames() + " ($" + transaction.getPerPersonAmount() + " each)";
                } else if ("SETTLEMENT".equals(transaction.getType())) {
                    details = "Paid to: " + transaction.getPayeeName();
                    if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
                        details += " - " + transaction.getNote();
                    }
                }
                table.addCell(new Cell().add(new Paragraph(details)));
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        }
    }
}
