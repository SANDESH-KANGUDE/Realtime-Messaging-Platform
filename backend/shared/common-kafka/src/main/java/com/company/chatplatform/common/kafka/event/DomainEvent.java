package com.company.chatplatform.common.kafka.event;

import com.company.chatplatform.common.core.util.UUIDv7Utils;
import java.time.Instant;

public class DomainEvent<T> {
    private String eventId;
    private String eventType;
    private String aggregateId;
    private String timestamp;
    private String version;
    private String correlationId;
    private T payload;

    public DomainEvent() {}

    public DomainEvent(String eventType, String aggregateId, String correlationId, T payload) {
        this.eventId = UUIDv7Utils.generateString();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.timestamp = Instant.now().toString();
        this.version = "1.0";
        this.correlationId = correlationId;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
