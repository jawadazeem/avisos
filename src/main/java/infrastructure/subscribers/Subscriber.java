/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package infrastructure.subscribers;

public interface Subscriber {
    void receiveUpdate(String update);
    int getId();
    SubscriberStatus getSubscriberStatus();
    SubscriberType getSubscriberType();
}
