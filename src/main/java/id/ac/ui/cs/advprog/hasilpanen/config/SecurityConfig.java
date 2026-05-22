package id.ac.ui.cs.advprog.hasilpanen.config;

import id.ac.ui.cs.advprog.hasilpanen.security.InternalApiTokenFilter;
import id.ac.ui.cs.advprog.hasilpanen.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider;
    private final ObjectProvider<InternalApiTokenFilter> internalApiTokenFilterProvider;

    public SecurityConfig(
            ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider,
            ObjectProvider<InternalApiTokenFilter> internalApiTokenFilterProvider
    ) {
        this.jwtAuthenticationFilterProvider = jwtAuthenticationFilterProvider;
        this.internalApiTokenFilterProvider = internalApiTokenFilterProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers("/harvests/**", "/mandor/**", "/harvest-reports/**", "/api/hasil-panen/**").authenticated()
                        .requestMatchers("/internal/**").authenticated()
                        .anyRequest().permitAll());

        InternalApiTokenFilter internalApiTokenFilter = internalApiTokenFilterProvider.getIfAvailable();
        if (internalApiTokenFilter != null) {
            http.addFilterBefore(internalApiTokenFilter, UsernamePasswordAuthenticationFilter.class);
        }

        JwtAuthenticationFilter jwtAuthenticationFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
