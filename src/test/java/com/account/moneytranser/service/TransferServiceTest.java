package com.account.moneytranser.service;

import com.account.moneytranser.entity.Account;
import com.account.moneytranser.entity.Transaction;
import com.account.moneytranser.exception.MoneyTransferException;
import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.repositories.AccountRepository;
import com.account.moneytranser.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private Executor customThreadPoolExecutor;


    @InjectMocks
    private TransferService transferService;

    private MoneyTransfer moneyTransfer;

    @BeforeEach
    void setUp() {
        customThreadPoolExecutor = Executors.newFixedThreadPool(10);
        transferService = new TransferService(accountRepository, transactionRepository, exchangeRateService, customThreadPoolExecutor);
    }

    @Test
    void testTransferSuccess() {
        Account fromAccount = Account.builder()
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .id(1L)
                .build();
        Account toAccount = Account.builder()
                .balance(new BigDecimal("500.00"))
                .currency("EUR")
                .id(2L)
                .build();


        moneyTransfer = new MoneyTransfer(1L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(exchangeRateService.getExchangeRate(anyString(), anyString())).thenReturn(Mono.just(new BigDecimal("0.85")));

        CompletableFuture<Void> future = transferService.transfer(moneyTransfer);

        future.join();

        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).findById(2L);
        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testTransferInsufficientBalance() {
        Account fromAccount = Account.builder()
                .balance(new BigDecimal("100.00"))
                .currency("USD")
                .id(1L)
                .build();

        Account toAccount = Account.builder()
                .balance(new BigDecimal("500.00"))
                .currency("EUR")
                .id(2L)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(exchangeRateService.getExchangeRate(anyString(), anyString())).thenReturn(Mono.just(new BigDecimal("0.85")));

        moneyTransfer = new MoneyTransfer(1L, 2L, new BigDecimal("200.00"));
        CompletableFuture<Void> future = transferService.transfer(moneyTransfer);
        ExecutionException executionException = assertThrows(ExecutionException.class, future::get);
        Throwable cause = executionException.getCause();
        assertThat(cause).isInstanceOf(MoneyTransferException.class);
        assertThat(cause.getMessage()).isEqualTo("Insufficient balance");
    }

    @Test
    void testTransferOptimisticLockingFailure() {

        Account fromAccount = Account.builder()
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .id(1L)
                .build();
        Account toAccount = Account.builder()
                .balance(new BigDecimal("500.00"))
                .currency("EUR")
                .id(2L)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(exchangeRateService.getExchangeRate(anyString(), anyString())).thenReturn(Mono.just(new BigDecimal("0.85")));
        doThrow(new OptimisticLockingFailureException("")).when(accountRepository).save(any(Account.class));

        moneyTransfer = new MoneyTransfer(1L, 2L, new BigDecimal("100.00"));
        CompletableFuture<Void> future = transferService.transfer(moneyTransfer);

        ExecutionException executionException = assertThrows(ExecutionException.class, future::get);
        Throwable cause = executionException.getCause();
        assertThat(cause).isInstanceOf(OptimisticLockingFailureException.class);
    }
}
