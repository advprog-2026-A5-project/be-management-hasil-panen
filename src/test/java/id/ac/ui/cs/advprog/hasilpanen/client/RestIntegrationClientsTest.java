package id.ac.ui.cs.advprog.hasilpanen.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
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
    void authClientParsesCurrentUserIdentity() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/api/users/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andRespond(withSuccess("""
                        {"id":21,"email":"buruh@mysawit.id","nama":"Buruh Test","role":"BURUH"}
                        """, MediaType.APPLICATION_JSON));

        var identity = client.getCurrentUserIdentity("Bearer token");
        assertThat(identity.id()).isEqualTo(21L);
        assertThat(identity.role()).isEqualTo("BURUH");
    }

    @Test
    void authClientFailsWhenCurrentUserResponseBodyEmpty() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/api/users/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getCurrentUserIdentity("Bearer token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("returned empty body");
    }

    @Test
    void authClientCallsSupervisorEndpointWithInternalToken() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate, "internal-token");

        server.expect(requestTo("http://auth/internal/buruh/17/supervisor"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(header("X-Internal-Service-Token", "internal-token"))
                .andRespond(withSuccess("""
                        {"buruhId":17,"buruhNama":"Buruh","mandorId":31,"mandorNama":"Mandor","active":true}
                        """, MediaType.APPLICATION_JSON));

        var supervisor = client.getBuruhSupervisor(17L, "Bearer token");
        assertThat(supervisor.active()).isTrue();
        assertThat(supervisor.mandorId()).isEqualTo(31L);
    }

    @Test
    void authClientFailsWhenSupervisorResponseBodyEmpty() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate, "internal-token");

        server.expect(requestTo("http://auth/internal/buruh/17/supervisor"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getBuruhSupervisor(17L, "Bearer token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("supervisor returned empty body");
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
    void authClientReturnsEmptySetWhenMandorListingBodyEmpty() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/internal/mandors/31/buruh"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        assertThat(client.getBuruhUnderMandor(31L, null)).isEmpty();
    }

    @Test
    void authClientCanMapLegacyAssignedBuruhIds() {
        AuthServiceRestClient client = new AuthServiceRestClient("http://auth", restTemplate);

        server.expect(requestTo("http://auth/internal/mandors/31/buruh"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":17,"role":"BURUH"}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getAssignedBuruhIds(id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.longToUuid(31L)))
                .containsExactly(id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge.longToUuid(17L));
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

    @Test
    void kebunClientRejectsMismatchedFarmCodeAccess() {
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);

        server.expect(requestTo("http://kebun/internal/mandors/31/kebun"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"mandorId\":31,\"active\":true,\"kebunCode\":\"KB001\"}", MediaType.APPLICATION_JSON));

        assertThat(client.hasFarmAccess(31L, "KB009", "Bearer token")).isFalse();
    }

    @Test
    void kebunClientReturnsInactiveAssignmentWhenNotFound() {
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);

        server.expect(requestTo("http://kebun/internal/mandors/31/kebun"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        var assignment = client.getKebunByMandor(31L, "Bearer token");
        assertThat(assignment.active()).isFalse();
        assertThat(assignment.kebunCode()).isNull();
    }

    @Test
    void kebunClientReturnsFalseForKebunCodeOnNotFound() {
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);

        server.expect(requestTo("http://kebun/kebun/KB404"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.kebunExistsByCode("KB404", "Bearer token")).isFalse();
    }

    @Test
    void kebunClientPropagatesServiceUnavailableAsRuntimeFailure() {
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);

        server.expect(requestTo("http://kebun/kebun/KB001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.kebunExistsByCode("KB001", "Bearer token"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void kebunClientReturnsFalseWhenKebunCodeBlank() {
        KebunServiceRestClient client = new KebunServiceRestClient("http://kebun", restTemplate);
        assertThat(client.kebunExistsByCode(" ", "Bearer token")).isFalse();
    }
}
