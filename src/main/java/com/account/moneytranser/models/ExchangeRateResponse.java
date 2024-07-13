package com.account.moneytranser.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ExchangeRateResponse {
    private String base;
    private Map<String, BigDecimal> rates;
}
