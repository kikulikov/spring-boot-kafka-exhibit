# Spring Boot & Kafka

A demo application utilising producer/consumer/admin API of Apache Kafka.

## Setting Up

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

## Running

Run `mvn clean package` to run tests, generate POJOs and build jar files. As a result you will get a fat jar like `/target/spring-boot-kafka-exhibit-0.0.1-SNAPSHOT.jar`.

Run the required application from the command line as

```
# Kafka Admin
java -cp target/spring-boot-kafka-exhibit-0.0.1-SNAPSHOT.jar -Dloader.main=io.confluent.admin.BasicAdminApplication org.springframework.boot.loader.PropertiesLauncher

# Kafka Producer
java -cp target/spring-boot-kafka-exhibit-0.0.1-SNAPSHOT.jar -Dloader.main=io.confluent.producer.BasicProducerApplication org.springframework.boot.loader.PropertiesLauncher

# Kafka Consumer
java -cp target/spring-boot-kafka-exhibit-0.0.1-SNAPSHOT.jar -Dloader.main=io.confluent.consumer.BasicConsumerApplication org.springframework.boot.loader.PropertiesLauncher

# Kafka Streams
java -cp target/spring-boot-kafka-exhibit-0.0.1-SNAPSHOT.jar -Dloader.main=io.confluent.streams.CountAndTotalApplication org.springframework.boot.loader.PropertiesLauncher
```

## Miscellaneous

* [Sending a Message](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka.sending)
* [Receiving a Message](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka.receiving)
* [Kafka Streams](https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.kafka.streams)

## Encryption

### Generating RSA keys

Generating RSA private and public keys.

```shell
openssl genpkey -algorithm RSA -outform PEM -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in privatekey.pem -out publickey.pem -pubout -outform PEM
```

Extracting RSA private and public keys.

```shell
cat private_key.pem | grep -v "KEY" | tr -d '\n'
cat public_key.pem | grep -v "KEY" | tr -d '\n'
```

### Adding Maven dependencies

`settings.xml` file to configure the private local maven repository

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <localRepository>./secret-m2-repository</localRepository>
    <interactiveMode>false</interactiveMode>
    <offline>false</offline>
</settings>
```

`pom.xml` needs to be updated with that private local maven repository

```xml
<repositories>
    <repository>
        <id>local-maven-repository</id>
        <url>file://${pom.basedir}/secret-m2-repository/</url>
        <releases>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </releases>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

and required `confluent-encryption` dependencies as

```xml
<properties>
    <encryption.version>2.0.3-cp-7.1</encryption.version>
</properties>
```

and 

```xml
<dependencies>
    <dependency>
        <groupId>io.confluent.confluent-encryption</groupId>
        <artifactId>confluent-encryption-common</artifactId>
        <version>${encryption.version}</version>
    </dependency>
    <dependency>
        <groupId>io.confluent.confluent-encryption</groupId>
        <artifactId>confluent-encryption-kafka</artifactId>
        <version>${encryption.version}</version>
    </dependency>
    <dependency>
        <groupId>io.confluent.confluent-encryption</groupId>
        <artifactId>confluent-encryption-serializer</artifactId>
        <version>${encryption.version}</version>
    </dependency>
</dependencies>
```

Finally, install the required libraries to the private local maven repository

```shell
mvn install:install-file -Dfile=confluent-encryption-common-2.0.3-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-common \
-Dversion=2.0.3-cp-7.1 -Dpackaging=jar --settings settings.xml

mvn install:install-file -Dfile=confluent-encryption-kafka-2.0.3-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-kafka \
-Dversion=2.0.3-cp-7.1 -Dpackaging=jar --settings settings.xml

mvn install:install-file -Dfile=confluent-encryption-serializer-2.0.3-cp-7.1.jar \
-DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-serializer \
-Dversion=2.0.3-cp-7.1 -Dpackaging=jar --settings settings.xml
```

Or just install the jar files to your local `~/.m2` maven: 

```shell
 mvn install:install-file -Dfile=confluent-encryption-common-2.0.3-cp-7.1/confluent-encryption-common-2.0.3-cp-7.1.jar \
 -DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-common -Dversion=2.0.3-cp-7.1 -Dpackaging=jar
 
 mvn install:install-file -Dfile=confluent-encryption-kafka-2.0.3-cp-7.1/confluent-encryption-kafka-2.0.3-cp-7.1.jar \
 -DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-kafka -Dversion=2.0.3-cp-7.1 -Dpackaging=jar
 
 mvn install:install-file -Dfile=confluent-encryption-serializer-2.0.3-cp-7.1/confluent-encryption-serializer-2.0.3-cp-7.1.jar \
 -DgroupId=io.confluent.confluent-encryption -DartifactId=confluent-encryption-serializer -Dversion=2.0.3-cp-7.1 -Dpackaging=jar
```