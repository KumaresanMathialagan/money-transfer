package com.account.moneytranser.service;

import com.account.moneytranser.entity.Account;
import com.account.moneytranser.repositories.AccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    @PostConstruct
    public void insertInitialData() {
        createAccounts(List.of(new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("750.00")),
                List.of("USD", "EUR", "GBP"));
    }

    private void createAccounts(List<BigDecimal> balances, List<String> currencies) {
        List<Account> accounts = IntStream.range(0, balances.size())
                .mapToObj(i -> getAccount(balances, currencies, i))
                .toList();
        accountRepository.saveAll(accounts);
    }

    private Account getAccount(List<BigDecimal> balances, List<String> currencies, int i) {
        return Account.builder()
                .balance(balances.get(i))
                .currency(currencies.get(i))
                .build();
    }

    public List<Account> getAccountDetails(List<Long> accounts) {
        return accounts.stream()
                .map(id -> accountRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Account not found for id: " + id)))
                .toList();
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }
}
