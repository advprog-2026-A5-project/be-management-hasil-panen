package id.ac.ui.cs.advprog.hasilpanen.config;

import id.ac.ui.cs.advprog.hasilpanen.service.HarvestPublicApiService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HarvestPublicApiConfiguration {

    @Bean
    @ConditionalOnMissingBean(HarvestPublicApiService.class)
    public HarvestPublicApiService harvestPublicApiService() {
        return new HarvestPublicApiService(null, null, null, null, null, null, null);
    }
}
