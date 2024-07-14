package com.account.moneytranser.service;

import com.account.moneytranser.entity.Account;
import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.repositories.AccountRepository;
import com.account.moneytranser.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private Executor taskExecutor;

    @BeforeEach
    void setup(){
        transferService = new TransferService(accountRepository,transactionRepository,new ExchangeRateService(webClientBuilder), taskExecutor);
    }

    @Test
    void testTransferSuccess() {
        Account fromAccount = new Account();
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setCurrency("USD");

        fromAccount = accountRepository.save(fromAccount);

        fromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();

        Account toAccount = new Account();
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setCurrency("EUR");
        toAccount = accountRepository.save(toAccount);

        BigDecimal amountToTransfer = new BigDecimal("100.00");
        MoneyTransfer moneyTransfer = new MoneyTransfer(fromAccount.getId(), toAccount.getId(), amountToTransfer);
        CompletableFuture<Void> future = transferService.transfer(moneyTransfer);

        future.join(); // Wait for the CompletableFuture to complete

        Account actualfromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account actualToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();

        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency()).doOnError(error -> {
                   throw new CompletionException(error);
        })
                .block();

        BigDecimal convertedAmount = amountToTransfer.multiply(exchangeRate);
        BigDecimal expectedFromAccountBalance = fromAccount.getBalance().subtract(amountToTransfer);
        BigDecimal expectedToAccountBalance = toAccount.getBalance().add(convertedAmount);

        assertThat(actualfromAccount.getBalance()).isEqualByComparingTo(expectedFromAccountBalance);
        assertThat(actualToAccount.getBalance()).isEqualByComparingTo(expectedToAccountBalance);
    }
}
