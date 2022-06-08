# Spring Boot & Kafka

A demo application utilising producer/consumer/admin API of Apache Kafka.

Docker Compose can be used to create a local Kafka environment. 
There is a docker compose configuration file which can be run from the command line as `docker-compose up` or `docker-compose up -d`.
Alternatively, you could use the Confluent CLI as `confluent local services kafka start`. 
For more information please see [command-reference/local/services](https://docs.confluent.io/confluent-cli/current/command-reference/local/services/index.html). 
Another option is to use [Confluent Cloud](https://confluent.cloud) where clusters are easy to spin up and tear down.

The application if using Avro and [Avro schemas](src/main/avro/PracticalSchemas.avsc). 
The POJO classes are generated automatically with the help of `avro-maven-plugin`.
Run the command `mvn generate-sources` to generate POJO classes.

The application configuration example can be observed at [application.yml](src/main/resources/application.yml). 
`KafkaAvroSerializer` is used for producer and consumer value serializers.

