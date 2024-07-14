# money-transfer

Transfer Controller process the api requests by two features. It will handle multiple requests at same time from multiple users
1. using **Async task executor** - @EnabledByDefault in MyFeatures.class
2. using **Rabbit MQ**

Switch between the required feature using MyFeatures.class

When using **Rabbit MQ** make sure you connect to rabbit mq service from your machine and set the below variables with appropriate values in application.yml
rabbitservice.enable: true
RABBITMQ_HOST: hostname
RABBITMQ_PORT: portnumber
RABBITMQ_USERNAME: username
RABBITMQ_PASSWORD: password

While starting application without proper connection to RabbitMQ service, application will display connection error but please ignore this and continue to hit the api using postman.

Steps to use postman to trigger transfer api

POST
http://localhost:8080/account/money/transfer
Body: in x-www-form-urlencoded
add params
Example:
fromAccountId : 1
toAccountId: 2
amount: 100