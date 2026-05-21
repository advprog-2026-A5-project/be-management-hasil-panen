package id.ac.ui.cs.advprog.hasilpanen.client;

import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AuthServiceRestClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public AuthServiceRestClient(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public HarvestPublicApiService.HarvestIdentity getCurrentUserIdentity(String bearerToken) {
        String url = baseUrl + "/api/users/me";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
        Map body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("auth /api/users/me returned empty body");
        }
        Number id = (Number) body.get("id");
        return new HarvestPublicApiService.HarvestIdentity(
                id == null ? null : id.longValue(),
                (String) body.get("email"),
                (String) body.get("nama"),
                (String) body.get("role"));
    }

    public HarvestPublicApiService.BuruhSupervisor getBuruhSupervisor(Long buruhId, String bearerToken) {
        String url = baseUrl + "/internal/buruh/" + buruhId + "/supervisor";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
        Map body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("auth /internal/buruh/{id}/supervisor returned empty body");
        }
        return new HarvestPublicApiService.BuruhSupervisor(
                toLong(body.get("buruhId")),
                (String) body.get("buruhNama"),
                toLong(body.get("mandorId")),
                (String) body.get("mandorNama"),
                Boolean.TRUE.equals(body.get("active")));
    }

    public boolean isBuruhAssignedToMandor(Long buruhId, Long mandorId, String bearerToken) {
        String url = baseUrl + "/internal/mandors/" + mandorId + "/buruh/" + buruhId + "/assignment";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
        Object assigned = response.getBody() == null ? null : response.getBody().get("assigned");
        return Boolean.TRUE.equals(assigned);
    }

    public Set<Long> getBuruhUnderMandor(Long mandorId, String bearerToken) {
        String url = baseUrl + "/internal/mandors/" + mandorId + "/buruh";
        ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map[].class);
        Map[] body = response.getBody();
        if (body == null) {
            return Set.of();
        }
        return java.util.Arrays.stream(body)
                .map(item -> (Number) item.get("id"))
                .filter(java.util.Objects::nonNull)
                .map(Number::longValue)
                .collect(Collectors.toSet());
    }

    private HttpEntity<Void> withBearer(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        return new HttpEntity<>(headers);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }
}
