package id.ac.ui.cs.advprog.hasilpanen.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestIntegrationClientsTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void authClientReturnsAssignmentTrue() {
        UUID mandorId = UUID.randomUUID();
        UUID buruhId = UUID.randomUUID();
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/internal/mandors/" + mandorId + "/buruh/" + buruhId + "/assignment"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("{\"assigned\":true}", MediaType.APPLICATION_JSON));

        boolean assigned = client.isBuruhAssignedToMandor(
                id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.uuidToLong(buruhId),
                id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.uuidToLong(mandorId),
                "Bearer token");

        assertThat(assigned).isTrue();
    }

    @Test
    void kebunClientReturnsAccessTrue() {
        UUID mandorId = UUID.randomUUID();
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);

        server.expect(requestTo("http://kebun/internal/mandors/" + mandorId + "/kebun"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("{\"active\":true,\"kebunCode\":\"KB001\"}", MediaType.APPLICATION_JSON));

        boolean hasAccess = client.hasFarmAccess(
                id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.uuidToLong(mandorId),
                "KB001",
                "Bearer token");

        assertThat(hasAccess).isTrue();
    }
}
