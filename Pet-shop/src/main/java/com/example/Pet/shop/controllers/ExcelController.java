package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.ProductRepository;
import com.example.Pet.shop.services.ExcelGeneration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/inventory")
public class ExcelController {

    private final ProductRepository productRepository;
    private final ExcelGeneration excelExportService;

    public ExcelController(ProductRepository productRepository, ExcelGeneration excelExportService) {
        this.productRepository = productRepository;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel() {
        try {
            List<Product> products = productRepository.findAll();
            byte[] excelContent = excelExportService.generateProductsExcel(products);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "products_report.xlsx");
            headers.setContentLength(excelContent.length);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(excelContent);

        } catch (IOException e) {
            String errorMessage = "Помилка генерації Excel: " + e.getMessage();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            return ResponseEntity
                    .internalServerError()
                    .headers(headers)
                    .body(errorMessage.getBytes(StandardCharsets.UTF_8));
        }
    }
}
