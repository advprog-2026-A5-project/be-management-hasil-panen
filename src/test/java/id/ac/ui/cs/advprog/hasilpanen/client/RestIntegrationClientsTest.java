package id.ac.ui.cs.advprog.hasilpanen.client;

import static org.assertj.core.api.Assertions.assertThat;

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
    void authClientUsesCanonicalLongIdsForAssignmentLookup() {
        long mandorId = 31L;
        long buruhId = 17L;
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/internal/mandors/31/buruh/17/assignment"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("{\"assigned\":true}", MediaType.APPLICATION_JSON));

        boolean assigned = client.isBuruhAssignedToMandor(buruhId, mandorId, "Bearer token");

        assertThat(assigned).isTrue();
    }

    @Test
    void authClientUsesCanonicalLongIdsForMandorBuruhListing() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/internal/mandors/31/buruh"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("""
                        [{"id":17,"role":"BURUH"},{"id":18,"role":"BURUH"}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getBuruhUnderMandor(31L, "Bearer token")).containsExactlyInAnyOrder(17L, 18L);
    }

    @Test
    void kebunClientUsesCanonicalLongIdsForFarmAccessLookup() {
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);

        server.expect(requestTo("http://kebun/internal/mandors/31/kebun"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("{\"mandorId\":31,\"active\":true,\"kebunCode\":\"KB001\"}", MediaType.APPLICATION_JSON));

        boolean hasAccess = client.hasFarmAccess(31L, "KB001", "Bearer token");

        assertThat(hasAccess).isTrue();
    }
}
