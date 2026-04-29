/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package service.receiver;
import core.SecurityHub;
import devices.api.DirectConnectDXHardwareLink;
import infrastructure.logger.LogLevel;
import infrastructure.logger.Logger;
import infrastructure.repository.SubscriberRepository;
import infrastructure.subscribers.Subscriber;

import java.util.List;
import java.util.Optional;

/**
 * Receiver and Subscriber both mean the same thing. Receiver is a type of Subscriber.
 */

public class ReceiverService {
    private final SubscriberRepository subscriberRepository;
    private final Logger logger;
    private final DirectConnectDXHardwareLink hardwareLink;
    private final SecurityHub hub;

    public ReceiverService(SecurityHub hub, SubscriberRepository subscriberRepository, DirectConnectDXHardwareLink hardwareLink, Logger logger) {
        this.subscriberRepository = subscriberRepository;
        this.logger = logger;
        this.hardwareLink = hardwareLink;
        this.hub = hub;
    }

    public void connectNewReceiver(int Id) {
        hardwareLink.enableLink(Id);
    }

    public List<Subscriber> getReceivers() {
        return subscriberRepository.loadAll();
    }

    public Optional<Subscriber> getReceiverById(int Id) {
        for (Subscriber s : getReceivers()) {
            if (s.getId() == Id) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public void removeReceiver(Subscriber subscriber) {
        if (getReceivers().contains(subscriber)) {
            subscriberRepository.remove(subscriber);
            hub.removeReceiver(subscriber);
            logger.log(subscriber + " was removed as a receiver", LogLevel.INFO);
        } else {
            logger.log(subscriber + " is not a receiver to be removed", LogLevel.ERROR);
        }
    }

    public void saveReceiver(Subscriber subscriber) {
        subscriberRepository.save(subscriber);
        hub.addReceiver(subscriber);
        logger.log(subscriber + " was saved as a new receiver", LogLevel.INFO);
    }

    public void clearReceivers() {
        subscriberRepository.removeAll();
        for (Subscriber s : hub.getSubscribers()) {
            hub.getSubscribers().remove(s);
        }
        logger.log("All receivers were cleared. There are zero active receivers", LogLevel.INFO);
    }
}
