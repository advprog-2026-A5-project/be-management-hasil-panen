package id.ac.ui.cs.advprog.hasilpanen.service;

import java.util.ArrayList;
import java.util.List;

class RecordingEventPublisherTransport implements EventPublisherTransport {

    private final List<OutboxEvent> publishedEvents = new ArrayList<>();
    private final boolean fail;

    RecordingEventPublisherTransport(boolean fail) {
        this.fail = fail;
    }

    @Override
    public void publish(OutboxEvent event) {
        if (fail) {
            throw new RuntimeException("publish failed");
        }
        publishedEvents.add(event);
    }

    List<OutboxEvent> publishedEvents() {
        return List.copyOf(publishedEvents);
    }
}
