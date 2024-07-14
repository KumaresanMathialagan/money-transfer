package com.account.moneytranser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@EnableJpaRepositories
@ComponentScan(basePackages = "com.account")
@SpringBootApplication
public class MoneyTranserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyTranserApplication.class, args);
    }

}
