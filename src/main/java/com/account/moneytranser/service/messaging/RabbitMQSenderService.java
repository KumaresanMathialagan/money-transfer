package com.account.moneytranser.service.messaging;

import com.account.moneytranser.models.MoneyTransfer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQSenderService {
    @Value("${messaging.transfer.exchange-name}")
    private String exchangeName;

    @Value("${messaging.transfer.routing-key}")
    private String moneyTransferRoutingKey;

    private final RabbitTemplate template;

    @Autowired
    public RabbitMQSenderService(RabbitTemplate template) {
        this.template = template;
    }

    public void sendToMoneyTransferQueue(MoneyTransfer moneyTransfer) {
        template.convertAndSend(exchangeName, moneyTransferRoutingKey, moneyTransfer);
    }
}
