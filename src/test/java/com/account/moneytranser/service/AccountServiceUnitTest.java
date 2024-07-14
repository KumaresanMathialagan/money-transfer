package com.account.moneytranser.service;

import com.account.moneytranser.entity.Account;
import com.account.moneytranser.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testGetAccountDetails() {

        long accountId = 1L;
        Account mockAccount = Account.builder().id(accountId).balance(new BigDecimal("1000.00")).currency("USD").build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        Account result = accountService.getAccountDetails(List.of(accountId)).get(0);

        assertThat(result.getId()).isEqualTo(accountId);
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.getCurrency()).isEqualTo("USD");

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testGetAccountDetailsNotFound() {

        long accountId = 999L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            accountService.getAccountDetails(List.of(accountId));
        });

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testCreateAccount() {

        Account newAccount = Account.builder().balance(new BigDecimal("500.00")).currency("EUR").build();
        Account savedAccount = Account.builder().id(1L).balance(new BigDecimal("500.00")).currency("EUR").build();

        when(accountRepository.save(newAccount)).thenReturn(savedAccount);

        Account result = accountService.createAccount(newAccount);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("500.00"));
        assertThat(result.getCurrency()).isEqualTo("EUR");

        verify(accountRepository, times(1)).save(newAccount);
    }
}
