package id.ac.ui.cs.advprog.hasilpanen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpPayrollEventPublisher implements EventPublisherTransport {

    private static final Logger log = LoggerFactory.getLogger(HttpPayrollEventPublisher.class);

    private final RestTemplate restTemplate;
    private final String paymentServiceBaseUrl;
    private final String payrollEndpoint;
    private final String fallbackAuthorization;

    public HttpPayrollEventPublisher(
            RestTemplate restTemplate,
            @Value("${payment.service.base-url:${PAYMENT_SERVICE_BASE_URL:http://localhost:8083}}") String paymentServiceBaseUrl,
            @Value("${payroll.endpoint:${PAYROLL_ENDPOINT:/api/payroll/harvest-events}}") String payrollEndpoint,
            @Value("${payment.service.auth-token:${PAYMENT_SERVICE_AUTH_TOKEN:}}") String fallbackAuthorization) {
        this.restTemplate = restTemplate;
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
        this.payrollEndpoint = payrollEndpoint;
        this.fallbackAuthorization = fallbackAuthorization;
    }

    @Override
    @Async
    public void publish(OutboxEvent event) {
        String url = paymentServiceBaseUrl + payrollEndpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (fallbackAuthorization != null && !fallbackAuthorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, fallbackAuthorization);
        }
        HttpEntity<String> request = new HttpEntity<>(event.payload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("payroll service returned non-2xx status");
        }
        log.debug("published payroll event {}", event.eventId());
    }
}
