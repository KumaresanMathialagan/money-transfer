package com.account.moneytranser.service.messaging;

import com.account.moneytranser.models.MoneyTransfer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMQSenderServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RabbitMQSenderService rabbitMQSenderService;

    @Captor
    private ArgumentCaptor<String> exchangeNameCaptor;

    @Captor
    private ArgumentCaptor<String> routingKeyCaptor;

    @Captor
    private ArgumentCaptor<MoneyTransfer> moneyTransferCaptor;

    @BeforeEach
    void setUp() {
        rabbitMQSenderService = new RabbitMQSenderService(rabbitTemplate, objectMapper);
        rabbitMQSenderService.setExchangeName("test.exchange"); // Setting exchange name for testing
        rabbitMQSenderService.setMoneyTransferRoutingKey("test.routingKey"); // Setting routing key for testing
    }

    @Test
    void testSendToMoneyTransferQueue() {
       MoneyTransfer moneyTransfer = new MoneyTransfer(1L,2L,new BigDecimal("100.00"));

        rabbitMQSenderService.sendToMoneyTransferQueue(moneyTransfer);

        verify(rabbitTemplate, times(1)).convertAndSend(
                exchangeNameCaptor.capture(),
                routingKeyCaptor.capture(),
                moneyTransferCaptor.capture()
        );

        assertEquals("test.exchange", exchangeNameCaptor.getValue());
        assertEquals("test.routingKey", routingKeyCaptor.getValue());
        assertEquals(moneyTransfer, moneyTransferCaptor.getValue());
    }
}