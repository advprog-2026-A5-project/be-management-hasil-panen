package id.ac.ui.cs.advprog.hasilpanen.service;

public interface EventPublisherTransport {

    void publish(OutboxEvent event);
}
