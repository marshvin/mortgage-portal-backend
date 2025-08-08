package com.mortgage.mortgageportal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mortgage.mortgageportal.event.ApplicationEventPayload;
import com.mortgage.mortgageportal.event.EventMetadata;
import com.mortgage.mortgageportal.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherServiceImpl implements EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_TOPIC = "loan.applications";
    private static final String TRACE_ID_HEADER = "traceId";

    @Override
    public void publishApplicationEvent(String topic, String key, ApplicationEventPayload payload) {
        publishApplicationEvent(topic, key, payload, payload.getMetadata().getTraceId());
    }

    @Override
    public void publishApplicationEvent(String topic, String key, ApplicationEventPayload payload, String traceId) {
        try {
            // Try Kafka first
            publishToKafka(topic, key, payload, traceId);
        } catch (Exception kafkaException) {
            log.warn("Failed to publish to Kafka: {}", kafkaException.getMessage());
            try {
                // Fallback to ActiveMQ
                publishToActiveMQ(topic, key, payload, traceId);
            } catch (Exception jmsException) {
                log.error("Failed to publish to both Kafka and ActiveMQ. Kafka error: {}, JMS error: {}", 
                         kafkaException.getMessage(), jmsException.getMessage());
                throw new RuntimeException("Failed to publish event to any messaging system", jmsException);
            }
        }
    }

    private void publishToKafka(String topic, String key, ApplicationEventPayload payload, String traceId) {
        try {
            // Add traceId to Kafka headers for OpenTelemetry
            List<Header> headers = List.of(
                new RecordHeader(TRACE_ID_HEADER, traceId.getBytes(StandardCharsets.UTF_8))
            );

            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, null, key, payload, headers);
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published to Kafka - Topic: {}, Key: {}, Partition: {}, Offset: {}", 
                            topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish to Kafka - Topic: {}, Key: {}", topic, key, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing to Kafka - Topic: {}, Key: {}", topic, key, e);
            throw e;
        }
    }

    private void publishToActiveMQ(String topic, String key, ApplicationEventPayload payload, String traceId) {
        try {
            // Create a wrapper object that includes the key and traceId
            ActiveMQMessageWrapper wrapper = ActiveMQMessageWrapper.builder()
                    .key(key)
                    .traceId(traceId)
                    .payload(payload)
                    .build();

            jmsTemplate.convertAndSend(topic, wrapper);
            log.info("Successfully published to ActiveMQ - Topic: {}, Key: {}", topic, key);
            
        } catch (Exception e) {
            log.error("Error publishing to ActiveMQ - Topic: {}, Key: {}", topic, key, e);
            throw e;
        }
    }

    // Helper class for ActiveMQ messages
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ActiveMQMessageWrapper {
        private String key;
        private String traceId;
        private ApplicationEventPayload payload;
    }
} 