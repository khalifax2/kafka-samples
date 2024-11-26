package com.tylerskafka.products.controller;

import com.tylerskafka.products.model.CreateProductModel;
import com.tylerskafka.products.model.ErrorMessage;
import com.tylerskafka.products.service.ProductService;
import com.tylerskafka.products.service.ProductServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    private ResponseEntity<Object> createProduct(@RequestBody CreateProductModel createProductModel) {

        String productId = null;
        try {
            productId = productService.createProduct(createProductModel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(new Date(), e.getMessage(), "/products"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }
}
