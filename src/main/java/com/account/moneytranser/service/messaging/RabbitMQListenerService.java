package com.account.moneytranser.service.messaging;

import com.account.moneytranser.models.MoneyTransfer;
import com.account.moneytranser.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListenerService {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListenerService.class);

    @Autowired
    private TransferService transferService;

    @RabbitListener(queues = "${messaging.transfer.queue-name}", concurrency = "1")
    public void moneyTransferQueue(MoneyTransfer moneyTransfer) {
        logger.info("Transfer request received from Rabbit Mq: fromAccountId={}, toAccountId={}, amount={}",
                moneyTransfer.fromAccount(), moneyTransfer.toAccount(), moneyTransfer.amount());
        transferService.transfer(moneyTransfer);
    }
}
