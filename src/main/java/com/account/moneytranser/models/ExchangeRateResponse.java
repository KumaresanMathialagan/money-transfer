package com.account.moneytranser.models;

import java.math.BigDecimal;
import java.util.Map;


public record ExchangeRateResponse(String base,
                                   Map<String, BigDecimal> rates) {

}
