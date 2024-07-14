package com.account.moneytranser.models;

import java.math.BigDecimal;

public record MoneyTransfer(Long fromAccount,
                            Long toAccount,
                            BigDecimal amount) {
}
