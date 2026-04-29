/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package infrastructure.subscribers;

public interface SmsSendable {
    void sendLatestUpdateAsSms();
    void sendUpdateAsSms(String update);
}
