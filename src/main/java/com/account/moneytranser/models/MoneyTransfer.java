package com.account.moneytranser.models;

import com.account.moneytranser.entity.Account;

import java.math.BigDecimal;

public record MoneyTransfer(Long fromAccount,
                            Long toAccount,
                            BigDecimal amount) {
}
