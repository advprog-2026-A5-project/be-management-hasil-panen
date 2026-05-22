package id.ac.ui.cs.advprog.hasilpanen.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

class HttpPayrollEventPublisherTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void publishSucceedsWhenPayrollServiceReturns2xx() {
        HttpPayrollEventPublisher publisher = new HttpPayrollEventPublisher(
                restTemplate,
                "http://payment",
                "/api/payroll/harvest-events",
                "Bearer service-token");
        OutboxEvent event = event("{\"harvestId\":\"abc\"}");

        server.expect(requestTo("http://payment/api/payroll/harvest-events"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer service-token"))
                .andExpect(content().json("{\"harvestId\":\"abc\"}"))
                .andRespond(withSuccess("ok", MediaType.APPLICATION_JSON));

        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
    }

    @Test
    void publishThrowsWhenPayrollServiceReturnsNon2xx() {
        HttpPayrollEventPublisher publisher = new HttpPayrollEventPublisher(
                restTemplate,
                "http://payment",
                "/api/payroll/harvest-events",
                "");
        OutboxEvent event = event("{\"harvestId\":\"abc\"}");

        server.expect(requestTo("http://payment/api/payroll/harvest-events"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(HttpServerErrorException.BadGateway.class);
    }

    @Test
    void publishThrowsIllegalStateWhenCustomErrorHandlerAllowsNon2xxResponse() {
        RestTemplate permissiveTemplate = new RestTemplate();
        permissiveTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(org.springframework.http.HttpStatusCode statusCode) {
                return false;
            }
        });
        MockRestServiceServer permissiveServer = MockRestServiceServer.createServer(permissiveTemplate);
        HttpPayrollEventPublisher publisher = new HttpPayrollEventPublisher(
                permissiveTemplate,
                "http://payment",
                "/api/payroll/harvest-events",
                "");
        OutboxEvent event = event("{\"harvestId\":\"abc\"}");

        permissiveServer.expect(requestTo("http://payment/api/payroll/harvest-events"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-2xx");
    }

    private OutboxEvent event(String payload) {
        return new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HarvestReport",
                "harvest.approved.v1",
                payload,
                "PENDING",
                Instant.now(),
                null,
                0);
    }
}
