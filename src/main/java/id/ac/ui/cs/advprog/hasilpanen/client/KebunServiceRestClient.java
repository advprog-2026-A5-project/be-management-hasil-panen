package id.ac.ui.cs.advprog.hasilpanen.client;

import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import java.util.Map;
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

    public HarvestPublicApiService.MandorKebunAssignment getKebunByMandor(Long mandorId, String bearerToken) {
        String url = baseUrl + "/internal/mandors/" + mandorId + "/kebun";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
        Map body = response.getBody();
        if (body == null) {
            return new HarvestPublicApiService.MandorKebunAssignment(mandorId, null, null, null, false);
        }
        return new HarvestPublicApiService.MandorKebunAssignment(
                toLong(body.get("mandorId")),
                (String) body.get("kebunId"),
                (String) body.get("kebunCode"),
                (String) body.get("kebunName"),
                Boolean.TRUE.equals(body.get("active")));
    }

    public boolean hasFarmAccess(Long mandorId, String kebunCode, String bearerToken) {
        HarvestPublicApiService.MandorKebunAssignment assignment = getKebunByMandor(mandorId, bearerToken);
        return assignment.active() && kebunCode != null && kebunCode.equals(assignment.kebunCode());
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
