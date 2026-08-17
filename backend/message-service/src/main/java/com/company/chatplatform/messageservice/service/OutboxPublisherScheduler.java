package com.company.chatplatform.messageservice.service;

import com.company.chatplatform.messageservice.domain.document.OutboxMessageDocument;
import com.company.chatplatform.messageservice.domain.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherScheduler(OutboxMessageRepository outboxMessageRepository,
                                  @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 200)
    public void publishPendingEvents() {
        List<OutboxMessageDocument> pending = outboxMessageRepository.findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, 50));
        if (pending.isEmpty()) {
            return;
        }

        for (OutboxMessageDocument event : pending) {
            try {
                if (kafkaTemplate != null) {
                    kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                    log.info("Published Mongo outbox message [{}] to topic {}", event.getId(), event.getEventType());
                } else {
                    log.debug("KafkaTemplate not active; marking Mongo outbox message [{}] SENT for dev mode", event.getId());
                }
                event.setStatus("SENT");
            } catch (Exception e) {
                log.error("Failed to publish Mongo outbox message [{}]", event.getId(), e);
                event.setStatus("FAILED");
            }
            outboxMessageRepository.save(event);
        }
    }
}
