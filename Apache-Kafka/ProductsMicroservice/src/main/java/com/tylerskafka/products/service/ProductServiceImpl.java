package com.tylerskafka.products.service;

import com.tylerskafka.core.producer.ProductCreatedEvent;
import com.tylerskafka.products.model.CreateProductModel;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ProductServiceImpl implements ProductService {

    private KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductModel createProductModel) throws Exception {
        String productId = UUID.randomUUID().toString();

        ProductCreatedEvent productCreatedEvent =
                new ProductCreatedEvent(productId, createProductModel.getTitle(), createProductModel.getPrice(), createProductModel.getQuantity());

//        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
//                kafkaTemplate.send("product-create-events-topic", productId, productCreatedEvent);
//
//        future.whenComplete((result, exception) -> {
//            if (exception != null) {
//                logger.error(">>> Failed to send the message: " + exception.getMessage());
//            } else {
//                logger.info(">>> Message sent successfully: " + result.getRecordMetadata());
//            }
//        });
        logger.info(">>> Before publishing ProductCreatedEvent");

        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>("product-created-events-topic", productId, productCreatedEvent);
        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(record).get();

//        SendResult<String, ProductCreatedEvent> result =
//                kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent).get();

        logger.info(">>> Topic " + result.getRecordMetadata().topic());
        logger.info(">>> Partition " + result.getRecordMetadata().partition());
        logger.info(">>> Offset " + result.getRecordMetadata().offset());

        logger.info(">>> Returning product id " + productId);
        return productId;
    }
}
