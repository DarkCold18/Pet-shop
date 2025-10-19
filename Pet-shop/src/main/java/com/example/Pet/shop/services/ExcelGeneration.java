package com.example.Pet.shop.services;

import com.example.Pet.shop.models.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelGeneration {
    public byte[] generateProductsExcel(List<Product> products) throws IOException {
        // Створення нової робочої книги (Workbook)
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Звіт про товари");
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        String[] headers = {"ID", "Назва", "Ціна (грн)", "Наявність", "Категорія", "Коротка назва", "Рекомендовано"};
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            // ID
            row.createCell(0).setCellValue(product.getId());

            // Назва
            row.createCell(1).setCellValue(product.getName());

            // Ціна
            Cell priceCell = row.createCell(2);
            priceCell.setCellValue(product.getPrice());

            // Наявність
            row.createCell(3).setCellValue(product.getQuantity());

            // Категорія
            row.createCell(4).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "Без категорії");

            // Короткий опис
            row.createCell(5).setCellValue(product.getShort_description());

            // Рекомендовано
            row.createCell(6).setCellValue(product.isRecommended() ? "Так" : "Ні");
        }

        //  Автоматична ширина колонок
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Запис у масив байтів
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
}