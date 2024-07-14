# money-transfer

Transfer Controller process the api requests by two features
1. using Async executor - @EnabledByDefault in MyFeatures.class
2. using Rabbit MQ

Switch between the required way using MyFeatures.class

When using Rabbit MQ make sure you connect to rabbit mq service from your machine and set the below variables with appropriate  values
RABBITMQ_HOST: hostname
RABBITMQ_PORT: portnumber
RABBITMQ_USERNAME: username
RABBITMQ_PASSWORD: password
