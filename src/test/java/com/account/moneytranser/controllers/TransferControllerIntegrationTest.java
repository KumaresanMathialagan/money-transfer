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
import java.util.List;

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
    public void testCreateAccounts() throws Exception {
        BigDecimal balance1 = new BigDecimal("1000.0");
        String currency1 = "USD";
        String username1 = "Kumaresan";

        BigDecimal balance2 = new BigDecimal("1500.0");
        String currency2 = "EUR";
        String username2 = "Balaji";

        List<Account> accounts = List.of(
                new Account(1L, username1, balance1, currency1),
                new Account(2L, username2, balance2, currency2)
        );

        String jsonContent = new ObjectMapper().writeValueAsString(accounts);

        mockMvc.perform(post("/accounts/create")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value(username1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].balance").value(1000.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].currency").value(currency1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].username").value(username2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].balance").value(1500.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].currency").value(currency2));
    }

    @Test
    public void testTransferMoney() throws Exception {

        Account fromAccount = Account.builder().username("Kumaresan").balance(new BigDecimal("1000.00")).currency("USD").build();
        Account toAccount = Account.builder().username("Karthick").balance(new BigDecimal("500.00")).currency("EUR").build();
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
        String username1 = "Kumaresan";
        String username2 = "Karthick";
        Account account1 = Account.builder().username(username1).balance(new BigDecimal("1000.0")).currency("USD").build();
        Account account2 = Account.builder().username(username2).balance(new BigDecimal("500.0")).currency("EUR").build();
        account1 = accountRepository.save(account1);
        account2 = accountRepository.save(account2);

        mockMvc.perform(get("/accounts/status")
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
