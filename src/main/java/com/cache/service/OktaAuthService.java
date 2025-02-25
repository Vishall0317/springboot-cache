package com.cache.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.cache.dto.OktaResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OktaAuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OktaAuthService.class);

    private WebClient webClient;
    private Cache<String, String> tokenCache;
//    private AtomicReference<String> oktaToken = new AtomicReference<>(null);
//    private long expiresIn = Long.parseLong(oktaToken.get().getExpires_in());

    public OktaAuthService(WebClient webClient) {
        this.webClient = webClient;
        this.tokenCache = createCache(10); // Default TTL (2 hours)
    }

    @Cacheable("oktaToken")
    public String getOktaToken() {
        log.debug("getting Okta token...");
        String cachedToken = tokenCache.getIfPresent("oktaToken");
        if (cachedToken != null) {
            log.debug("getting Okta token from cache...");
            return cachedToken;
        }
        return refreshToken();


//        log.debug("getting Okta token...");
//        if (oktaToken.get() == null) {
//            log.debug("getting Okta token from cache...");
//            refreshToken();
//        }
//        log.debug("Returning Okta token: {}", oktaToken.get());
//        return oktaToken.get().getAccess_token();
    }

    private Cache<String, String> createCache(int expiresInSeconds) {
        log.debug("create cache with expiresInSeconds: {}", expiresInSeconds);
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(1)
                .build();
    }

//    @Scheduled(fixedRate = 3600000) // 1 hour 58 minutes
    public String refreshToken() {
        log.debug("Refreshing Okta token");
        OktaResponse oktaResponse = webClient.post()
                .uri("https://dev-25386993.okta.com/oauth2/default/v1/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&scope=custom_scope")
                .headers(httpHeaders -> httpHeaders.setBasicAuth("0oani8wgz82BhldXe5d7", "MO1Td7QUceBBsFgjQL9kXl24Okqsk3YJqESv2L_L1984CpCm75p4Jug6xFB_MT4Q"))
                .retrieve()
                .bodyToMono(OktaResponse.class)
                .block();
        if (oktaResponse != null) {
            // Update cache with new TTL
            this.tokenCache = createCache(Integer.parseInt(oktaResponse.getExpires_in()));
            tokenCache.put("oktaToken", oktaResponse.getAccess_token());
            log.debug("Returning Okta token: {}", oktaResponse.getAccess_token());
            return oktaResponse.getAccess_token();
        }
        throw new RuntimeException("Failed to refresh Okta token");
    }



//    @CacheEvict(value = "oktaToken", allEntries = true)
//    @Scheduled(fixedRate = 60000) // 1 minute
//    public void clearCache() {
//        System.out.println("🗑️ Clearing Okta token from cache...");
//        oktaToken.set(null); // Also clear the atomic reference
//    }
}
