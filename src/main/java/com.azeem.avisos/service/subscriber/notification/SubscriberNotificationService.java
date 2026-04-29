/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.service.subscriber.notification;

import com.azeem.avisos.infrastructure.logger.LogLevel;
import com.azeem.avisos.infrastructure.logger.Logger;
import com.azeem.avisos.infrastructure.repository.SubscriberRepository;
import com.azeem.avisos.infrastructure.subscribers.Subscriber;

import java.util.List;

/**
 * Stateless Service Layer
 */
public class SubscriberNotificationService {
    private final SubscriberRepository subscriberRepository;
    private final Logger logger;

    public SubscriberNotificationService(SubscriberRepository subscriberRepository, Logger logger) {
        this.logger = logger;
        this.subscriberRepository = subscriberRepository;
    }

    public void sendToAll(String update) {
        List<Subscriber> subscribers = subscriberRepository.loadAll();
        for (Subscriber s : subscribers) {
            s.receiveUpdate(update);
        }
        logger.log(update + " was sent to all registered subscribers.", LogLevel.INFO);
    }

    public void removeSubscriber(Subscriber subscriber) {
        subscriberRepository.remove(subscriber);
        logger.log(subscriber + " was removed as a subscriber", LogLevel.INFO);
    }

    public void addSubscriber(Subscriber subscriber) {
        subscriberRepository.save(subscriber);
        logger.log(subscriber + " was added as a subscriber", LogLevel.INFO);
    }

    public List<Subscriber> getSubscribers() {
        return subscriberRepository.loadAll();
    }
}
