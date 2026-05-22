package id.ac.ui.cs.advprog.hasilpanen.client;

import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class KebunServiceRestClient implements KebunClient {

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public KebunServiceRestClient(
            @Value("${kebun.service.base-url:${KEBUN_SERVICE_BASE_URL:http://localhost:8081}}") String baseUrl,
            RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public HarvestPublicApiService.MandorKebunAssignment getKebunByMandor(Long mandorId, String bearerToken) {
        String url = baseUrl + "/internal/mandors/" + mandorId + "/kebun";
        try {
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
        } catch (HttpClientErrorException.NotFound ex) {
            return new HarvestPublicApiService.MandorKebunAssignment(mandorId, null, null, null, false);
        }
    }

    @Override
    public boolean hasFarmAccess(Long mandorId, String kebunCode) {
        return hasFarmAccess(mandorId, kebunCode, null);
    }

    @Override
    public boolean hasFarmAccess(Long mandorId, String kebunCode, String bearerToken) {
        HarvestPublicApiService.MandorKebunAssignment assignment = getKebunByMandor(mandorId, bearerToken);
        return assignment.active() && kebunCode != null && kebunCode.equals(assignment.kebunCode());
    }

    @Override
    public boolean kebunExistsByCode(String kebunCode, String bearerToken) {
        if (kebunCode == null || kebunCode.isBlank()) {
            return false;
        }
        String url = baseUrl + "/kebun/" + kebunCode;
        try {
            restTemplate.exchange(url, HttpMethod.GET, withBearer(bearerToken), Map.class);
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        }
    }

    private HttpEntity<Void> withBearer(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null && !bearerToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        }
        return new HttpEntity<>(headers);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }
}
