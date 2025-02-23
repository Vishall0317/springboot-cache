package com.cache.controller;


import com.cache.entity.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@RestController
@RequestMapping("/r/employees")
public class ReactiveEmployeeController {

    private WebClient webClient;

    public ReactiveEmployeeController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getEmployee(@PathVariable("id") int id) {
        Employee employee = new Employee();
        getEmployeeById(id)
                .subscribe(e -> System.out.println("Employee: " + employee));
        return new ResponseEntity<>("employee fetch with id: "+id, HttpStatus.OK);
    }

    public Mono<String> getEmployeeById(int id) {
        return webClient.get()
                .uri("http://localhost:8080/employees/{id}", id) // API Endpoint
                .retrieve()
                .bodyToMono(String.class) // Convert response to String
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(30))     // Maximum backoff 30s
                        .jitter(0.5)     // Add jitter (0.5 means ±50% randomness)
                        .filter(ex -> ex instanceof WebClientResponseException &&
                                ((WebClientResponseException) ex).getStatusCode().is5xxServerError())
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            return new RuntimeException("Retries exhausted: " + retrySignal.totalRetries());
                        }))
                .doOnError(error -> System.err.println("Error fetching employee: " + error.getMessage()));
    }
}
