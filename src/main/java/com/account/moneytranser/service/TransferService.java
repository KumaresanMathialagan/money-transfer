package com.account.moneytranser.service;


import com.account.moneytranser.entity.Account;
import com.account.moneytranser.entity.Transaction;
import com.account.moneytranser.exception.MoneyTransferException;
import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.repositories.AccountRepository;
import com.account.moneytranser.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

   private static final long BACKOFF_DELAY = 1000;


    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final Executor taskExecutor;

    public TransferService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           ExchangeRateService exchangeRateService,
                           Executor taskExecutor) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
        this.taskExecutor = taskExecutor;
    }

    @Transactional
    @Retryable(value = { OptimisticLockingFailureException.class }, backoff = @Backoff(delay = BACKOFF_DELAY))
    public CompletableFuture<Void> transfer(MoneyTransfer moneyTransfer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Initiating transfer: fromAccountId={}, toAccountId={}, amount={}", moneyTransfer.fromAccount(), moneyTransfer.toAccount(), moneyTransfer.amount());

                processTransfer(moneyTransfer);

            } catch (OptimisticLockingFailureException e) {
                logger.error("Transfer failed: {}", e.getMessage());
                throw e; // Optionally rethrow or handle the exception as needed
            } catch (MoneyTransferException e) {
                logger.error("Transfer failed: {}", e.getMessage());
                throw e; // No retry for business logic exceptions
            }catch (Exception e) {
                logger.error("Transfer failed", e);
                throw new MoneyTransferException("Transfer failed", e); // Handle unexpected exceptions
            }
            return null;
        },taskExecutor);
    }

    private void processTransfer(MoneyTransfer moneyTransfer) {
        Optional<Account> fromAccountOpt = accountRepository.findById(moneyTransfer.fromAccount());
        Optional<Account> toAccountOpt = accountRepository.findById(moneyTransfer.toAccount());

        if (fromAccountOpt.isEmpty() || toAccountOpt.isEmpty()) {
            throw new MoneyTransferException("Account not found");
        }

        Account fromAccount = fromAccountOpt.get();
        Account toAccount = toAccountOpt.get();

        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency())
                .doOnError(error -> {
                    logger.error("Failed to get exchange rate", error);
                    throw new CompletionException(error);
                })
                .block(); // block() to get the result synchronously

        BigDecimal convertedAmount = moneyTransfer.amount().multiply(exchangeRate);

        if (fromAccount.getBalance().compareTo(moneyTransfer.amount()) < 0) {
            throw new MoneyTransferException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(moneyTransfer.amount()));
        toAccount.setBalance(toAccount.getBalance().add(convertedAmount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setFromAccountId(moneyTransfer.fromAccount());
        transaction.setToAccountId(moneyTransfer.toAccount());
        transaction.setAmount(moneyTransfer.amount());
        transaction.setExchangeRate(exchangeRate);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        logger.info("Transfer completed successfully");
    }

}