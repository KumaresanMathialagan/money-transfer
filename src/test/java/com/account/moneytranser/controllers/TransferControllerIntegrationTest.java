package com.account.moneytranser.controllers;


import com.account.moneytranser.entity.Account;
import com.account.moneytranser.repositories.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    public void testCreateAccount() throws Exception {
        BigDecimal balance = new BigDecimal("1000.0");
        String currency = "USD";

        mockMvc.perform(post("/account/create")
                        .param("balance", balance.toString())
                        .param("currency", currency)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(1000.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.currency").value(currency));
    }

    @Test
    public void testTransferMoney() throws Exception {
        Account fromAccount = Account.builder().balance(new BigDecimal("1000.00")).currency("USD").build();
        Account toAccount = Account.builder().balance(new BigDecimal("500.00")).currency("EUR").build();
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        mockMvc.perform(post("/account/money/transfer")
                        .param("fromAccountId", fromAccount.getId().toString())
                        .param("toAccountId", toAccount.getId().toString())
                        .param("amount", "100.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Transfer initiated. Processing in background.")));
    }

    @Test
    public void testGetAllAccounts() throws Exception {
        Account account1 = Account.builder().balance(new BigDecimal("1000.0")).currency("USD").build();
        Account account2 = Account.builder().balance(new BigDecimal("500.0")).currency("EUR").build();
        account1 = accountRepository.save(account1);
        account2 = accountRepository.save(account2);

        mockMvc.perform(get("/accounts")
                        .param("fromAccountId", account1.getId().toString())
                        .param("toAccountId", account2.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(account1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(account1.getBalance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency").value(account1.getCurrency()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(account2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].balance").value(account2.getBalance()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].currency").value(account2.getCurrency()));
    }

}
