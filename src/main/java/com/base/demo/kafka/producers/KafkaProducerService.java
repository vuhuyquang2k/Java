package com.base.demo.kafka.producers;

import com.base.demo.configs.kafka.KafkaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    @Qualifier(KafkaConfig.KAFKA_TEMPLATE)
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void send(String topic, Object key, Object data) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, toJson(key), toJson(data));
        future.whenComplete((result, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleMessageFailure(topic, throwable);
            } else {
                handleMessageSuccess(topic, key);
            }
        });
    }

    public void send(String topic, Object key, String data) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, toJson(key), data);
        future.whenComplete((result, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleMessageFailure(topic, throwable);
            } else {
                handleMessageSuccess(topic, key);
            }
        });
    }

    public void send(String topic, Object data) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, toJson(data));
        future.whenComplete((result, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleMessageFailure(topic, throwable);
            } else {
                handleMessageSuccess(topic, "");
            }
        });
    }

    public void send(String topic, String data) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, data);
        future.whenComplete((result, throwable) -> {
            if (Objects.nonNull(throwable)) {
                handleMessageFailure(topic, throwable);
            } else {
                handleMessageSuccess(topic, "");
            }
        });
    }

    private void handleMessageFailure(String topic, Throwable throwable) {
        log.warn("[Kafka-Producer] Send message to topic {} failed: {}",
                topic, throwable.getMessage());
    }

    private void handleMessageSuccess(String topic, Object key) {
        log.info("[Kafka-Producer] Send message to topic {} with key {} success",
                topic, toJson(key));
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[Kafka-Producer] Failed to serialize: {}", e.getMessage());
            return obj.toString();
        }
    }
}