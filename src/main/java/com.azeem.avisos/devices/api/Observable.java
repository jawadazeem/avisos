/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.devices.api;

import com.azeem.avisos.infrastructure.subscribers.Subscriber;

public interface Observable {
    void addSubscriber(Subscriber s);
    void removeSubscriber(Subscriber s);
    void updateSubscriber(Subscriber s, String updateMessage);
    void updateAllSubscribers(String updateMessage);
}
