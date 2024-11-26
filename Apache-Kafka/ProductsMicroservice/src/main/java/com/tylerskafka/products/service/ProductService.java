package com.tylerskafka.products.service;

import com.tylerskafka.products.model.CreateProductModel;

import java.util.concurrent.ExecutionException;

public interface ProductService {
    String createProduct(CreateProductModel createProductModel) throws Exception;
}
