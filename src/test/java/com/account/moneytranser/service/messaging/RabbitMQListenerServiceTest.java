package com.account.moneytranser.service.messaging;

import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitMQListenerServiceTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private RabbitMQListenerService rabbitMQListenerService;

    @Test
    void testMoneyTransferQueue() {
        MoneyTransfer moneyTransfer = new MoneyTransfer(1L, 2L, new BigDecimal("100.00"));

        rabbitMQListenerService.moneyTransferQueue(moneyTransfer);

        verify(transferService, times(1)).transfer(moneyTransfer);
    }
}
