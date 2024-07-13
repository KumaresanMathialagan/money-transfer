package com.account.moneytranser.service.messaging;

import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.service.TransferService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListenerService {

    @Autowired
    private TransferService transferService;

    @RabbitListener(queues = "${messaging.transfer.queue-name}", concurrency = "1")
    public void moneyTransferQueue(MoneyTransfer moneyTransfer) {
        transferService.transfer(moneyTransfer);
    }
}
