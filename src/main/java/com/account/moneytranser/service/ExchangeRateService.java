package com.account.moneytranser.service;


import com.account.moneytranser.models.ExchangeRateResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ExchangeRateService {

    private final WebClient webClient;

    public ExchangeRateService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.exchangerate-api.com/v4/latest").build();
    }

    public Mono<BigDecimal> getExchangeRate(String fromCurrency, String toCurrency) {
        return this.webClient.get()
                .uri("/{currency}", fromCurrency)
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .map(response -> response.rates().get(toCurrency));
    }
}
