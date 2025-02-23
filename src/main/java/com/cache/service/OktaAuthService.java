package com.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OktaAuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OktaAuthService.class);

    private WebClient webClient;
    private AtomicReference<String> oktaToken = new AtomicReference<>(null);

    public OktaAuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Cacheable("oktaToken")
    public String getOktaToken() {
        log.debug("getting Okta token...");
        if (oktaToken.get() == null) {
            log.debug("getting Okta token from cache...");
            refreshToken();
        }
        log.debug("Returning Okta token: {}", oktaToken.get());
        return oktaToken.get();
    }

//    @Scheduled(fixedRate = 6) // 1 hour 58 minutes
    public void refreshToken() {
        log.debug("Refreshing Okta token");
        String token = webClient.post()
                .uri("https://dev-25386993.okta.com/oauth2/default/v1/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&scope=custom_scope")
                .headers(httpHeaders -> httpHeaders.setBasicAuth("0oani8wgz82BhldXe5d7", "MO1Td7QUceBBsFgjQL9kXl24Okqsk3YJqESv2L_L1984CpCm75p4Jug6xFB_MT4Q"))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        oktaToken.set(token);
    }

    @CacheEvict(value = "oktaToken", allEntries = true)
    @Scheduled(fixedRate = 60000) // 1 minute
    public void clearCache() {
        System.out.println("🗑️ Clearing Okta token from cache...");
        oktaToken.set(null); // Also clear the atomic reference
    }
}
