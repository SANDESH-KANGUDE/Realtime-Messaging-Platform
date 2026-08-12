package com.company.chatplatform.authservice.service;

import com.company.chatplatform.authservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.authservice.domain.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherScheduler(OutboxEventRepository outboxEventRepository,
                                  @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEventEntity event : pendingEvents) {
            try {
                if (kafkaTemplate != null) {
                    kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                    log.info("Published outbox event [{}] to topic {}", event.getId(), event.getEventType());
                } else {
                    log.debug("KafkaTemplate not active; marking outbox event [{}] as SENT for dev mode", event.getId());
                }
                event.setStatus("SENT");
            } catch (Exception e) {
                log.error("Failed to publish outbox event [{}]", event.getId(), e);
                event.setStatus("FAILED");
            }
            outboxEventRepository.save(event);
        }
    }
}
