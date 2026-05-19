package id.ac.ui.cs.advprog.hasilpanen.client;

import id.ac.ui.cs.advprog.hasilpanen.service.LegacyIdBridge;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class KebunServiceRestClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public KebunServiceRestClient(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public boolean hasFarmAccess(Long mandorId, String kebunCode, String bearerToken) {
        UUID mandorUuid = LegacyIdBridge.longToUuid(mandorId);
        String url = baseUrl + "/internal/mandors/" + mandorUuid + "/kebun";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
        Map body = response.getBody();
        if (body == null) {
            return false;
        }
        boolean active = Boolean.TRUE.equals(body.get("active"));
        Object code = body.get("kebunCode");
        return active && kebunCode != null && kebunCode.equals(code);
    }

    private HttpEntity<Void> withBearer(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        return new HttpEntity<>(headers);
    }
}
