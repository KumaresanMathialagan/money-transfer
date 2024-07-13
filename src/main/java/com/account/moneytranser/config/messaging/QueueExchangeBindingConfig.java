package com.account.moneytranser.config.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueExchangeBindingConfig {

    @Value("${messaging.transfer.exchange-name}")
    private String exchangeName;

    @Value("${messaging.transfer.queue-name}")
    private String transferQueueName;

    @Value("${messaging.transfer.routing-key}")
    private String transferRoutingKey;

    @Value("${messaging.dlq.exchange-name}")
    private String dlqExchangeName;

    @Value("${messaging.dlq.queue-name}")
    private String dlqQueueName;

    @Bean
    TopicExchange transferExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Queue transferStartQueue() {
        return QueueBuilder.durable(transferQueueName)
                .withArgument("x-dead-letter-exchange", dlqExchangeName)
                .withArgument("x-dead-letter-routing-key", "dlq.transfer.start")
                .build();
    }

    @Bean
    Binding transferStartBinding(Queue transferStartQueue) {
        return BindingBuilder
                .bind(transferStartQueue)
                .to(new TopicExchange(exchangeName))
                .with(transferRoutingKey);
    }

    @Bean
    TopicExchange dlqExchange() {
        return new TopicExchange(dlqExchangeName);
    }

    @Bean
    Queue dlqQueue() {
        return QueueBuilder
                .durable(dlqQueueName)
                .build();
    }

    @Bean
    Binding dlqBinding(Queue dlqQueue, TopicExchange dlqExchange) {
        return BindingBuilder
                .bind(dlqQueue)
                .to(dlqExchange)
                .with("#");
    }
}
