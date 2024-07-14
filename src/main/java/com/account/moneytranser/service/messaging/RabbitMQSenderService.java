package com.account.moneytranser.service.messaging;

import com.account.moneytranser.models.MoneyTransfer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Getter
@Setter
public class RabbitMQSenderService {
    @Value("${messaging.transfer.exchange-name}")
    private String exchangeName;

    @Value("${messaging.transfer.routing-key}")
    private String moneyTransferRoutingKey;

    private final RabbitTemplate template;
    private final ObjectMapper objectMapper;


    @Autowired
    public RabbitMQSenderService(RabbitTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public void sendToMoneyTransferQueue(MoneyTransfer moneyTransfer) {
        template.convertAndSend(exchangeName, moneyTransferRoutingKey, moneyTransfer);
    }
}
