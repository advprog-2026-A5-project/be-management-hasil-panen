package id.ac.ui.cs.advprog.hasilpanen.client;

import id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    public boolean isBuruhAssignedToMandor(Long buruhId, Long mandorId, String bearerToken) {
        UUID buruhUuid = LegacyIdBridge.longToUuid(buruhId);
        UUID mandorUuid = LegacyIdBridge.longToUuid(mandorId);
        String url = baseUrl + "/internal/mandors/" + mandorUuid + "/buruh/" + buruhUuid + "/assignment";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
        Object assigned = response.getBody() == null ? null : response.getBody().get("assigned");
        return Boolean.TRUE.equals(assigned);
    }

    public Set<Long> getBuruhUnderMandor(Long mandorId, String bearerToken) {
        UUID mandorUuid = LegacyIdBridge.longToUuid(mandorId);
        String url = baseUrl + "/internal/mandors/" + mandorUuid + "/buruh";
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
}
