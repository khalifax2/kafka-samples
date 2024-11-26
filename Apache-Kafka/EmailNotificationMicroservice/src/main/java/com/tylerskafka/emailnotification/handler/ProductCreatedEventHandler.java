package com.tylerskafka.emailnotification.handler;

import com.tylerskafka.core.producer.ProductCreatedEvent;
import com.tylerskafka.emailnotification.error.NotRetryableException;
import com.tylerskafka.emailnotification.error.RetryableException;
import com.tylerskafka.emailnotification.model.ProcessedEventEntity;
import com.tylerskafka.emailnotification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@KafkaListener(topics="product-created-events-topic")
public class ProductCreatedEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProductCreatedEventHandler.class);
    private RestTemplate restTemplate;
    private ProcessedEventRepository processedEventRepository;

    public ProductCreatedEventHandler(RestTemplate restTemplate, ProcessedEventRepository processedEventRepository) {
        this.restTemplate = restTemplate;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @KafkaHandler
    public void handle(@Payload ProductCreatedEvent productCreatedEvent,
                       @Header("messageId") String messageId,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        logger.info("Received a new event: " + productCreatedEvent.getTitle() + " with productId : " + productCreatedEvent.getProductId());

        ProcessedEventEntity existingRecord = processedEventRepository.findByMessageId(messageId);
        if (existingRecord != null) {
            logger.info("Duplicate message id found : " + existingRecord.getMessageId());
            return;
        }

        String requestUrl = "http://localhost:8082/response/200";
        try {
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Received response from a remote service: " + response.getBody());
            }

        } catch (ResourceAccessException ex) {
            logger.error(ex.getMessage());
            throw new RetryableException(ex);
        } catch (HttpServerErrorException ex) {
            logger.error(ex.getMessage());
            throw new NotRetryableException(ex);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new NotRetryableException(ex);
        }

        try {
            processedEventRepository.save(new ProcessedEventEntity(messageId, productCreatedEvent.getProductId()));
        } catch (DataIntegrityViolationException ex) {
            throw new NotRetryableException(ex);
        }
    }
}
