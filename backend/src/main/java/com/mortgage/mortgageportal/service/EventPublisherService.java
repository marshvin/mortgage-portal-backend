package com.mortgage.mortgageportal.service;

import com.mortgage.mortgageportal.event.ApplicationEventPayload;

public interface EventPublisherService {
    void publishApplicationEvent(String topic, String key, ApplicationEventPayload payload);
    void publishApplicationEvent(String topic, String key, ApplicationEventPayload payload, String traceId);
} 