package com.account.moneytranser.controllers;

import com.account.moneytranser.entity.Account;
import com.account.moneytranser.feature.MyFeatures;
import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.repositories.AccountRepository;
import com.account.moneytranser.service.AccountService;
import com.account.moneytranser.service.TransferService;
import com.account.moneytranser.service.messaging.RabbitMQSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class TransferController {
    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RabbitMQSenderService rabbitMQSenderService;

    @Autowired
    private FeatureManager featureManager;

    @PostMapping("/account/money/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam Long fromAccountId,
            @RequestParam Long toAccountId,
            @RequestParam BigDecimal amount) {

        logger.info("Transfer request received: fromAccountId={}, toAccountId={}, amount={}", fromAccountId, toAccountId, amount);
        MoneyTransfer moneyTransfer = new MoneyTransfer(fromAccountId, toAccountId, amount);
        MyFeatures feature = featureManager.isActive(MyFeatures.ASYNC_EXECUTOR) ? MyFeatures.ASYNC_EXECUTOR : MyFeatures.RABBIT_MQ;

        switch (feature) {
            case ASYNC_EXECUTOR -> transferService.transfer(moneyTransfer);
            case RABBIT_MQ -> rabbitMQSenderService.sendToMoneyTransferQueue(moneyTransfer);
        }
        // Return immediately with "Transfer initiated" message
        return ResponseEntity.ok("Transfer initiated. Processing in background.");
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts(@RequestParam Long fromAccountId,
                                                        @RequestParam Long toAccountId) {

        List<Account> accounts = accountService.getAccountDetails(List.of(fromAccountId,toAccountId));
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/account/create")
    public ResponseEntity<Account> createAccount(
            @RequestParam BigDecimal balance,
            @RequestParam String currency) {

        Account account = Account.builder().balance(balance).currency(currency).build();
        Account savedAccount = accountService.createAccount(account);
        return ResponseEntity.ok(savedAccount);
    }

}