# Guide: Integrating Subject Synchronization

## Context
When microservices use `bedrock-authz-starter` for policy enforcement, the `authz-core` framework maintains a highly-available, local projection of subjects (Users and Roles). This allows the framework to validate that a subject exists before attaching policies to them, and allows the Admin UI to read subjects directly from the consumer service without synchronous cross-service lookups.

To keep this local projection up-to-date, the central **Identity Service** publishes subject lifecycle events in a standard JSON format via RabbitMQ on the `auth.subject.sync` Fanout exchange.

It is the responsibility of **each consuming microservice** (e.g., Pharmacy, Clinic, Restaurant) to implement an **Anti-Corruption Layer (ACL)**. The consumer must listen to this exchange, deserialize the JSON into its own local DTO, map it to the framework's internal event model, and pass it into the `authz-core` framework.

---

## 1. Add Dependencies

Ensure your microservice has the AMQP starter, `bedrock-authz-starter`, and Pact for contract testing in its `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.datamate.authz.starter</groupId>
    <artifactId>bedrock-authz-starter</artifactId>
</dependency>
<!-- For Contract Testing -->
<dependency>
    <groupId>au.com.dius.pact.consumer</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.3</version>
    <scope>test</scope>
</dependency>
```

---

## 2. Define the ACL DTO

Create a local DTO in your service that models the JSON wire contract defined in `docs/contracts/subject-sync-wire-contract.md`. 
**Do not import any DTO from the Identity Service.**

```java
package org.datamate.your_service.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubjectSyncMessage(
        String subjectType,
        String subjectId,
        String subjectName,
        String displayName,
        String email,
        String description,
        String status,
        long version,
        boolean deleted
) {}
```

---

## 3. Implement the Listener & Mapper

Create a standard RabbitMQ listener in your microservice. You need to map your local `SubjectSyncMessage` to `AuthzSubjectSyncEvent` and inject the `SubjectManagementService` (provided by `authz-core`) to call its `apply()` method.

```java
package org.datamate.your_service.messaging;

import org.datamate.authz.api.subject.SubjectManagementService;
import org.datamate.authz.event.AuthzSubjectSyncEvent;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SubjectSyncRabbitListener {

    private static final Logger log = LoggerFactory.getLogger(SubjectSyncRabbitListener.class);
    private final SubjectManagementService subjectManagementService;

    public SubjectSyncRabbitListener(SubjectManagementService subjectManagementService) {
        this.subjectManagementService = subjectManagementService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue, // Anonymous auto-delete queue
            exchange = @Exchange(value = "auth.subject.sync", type = ExchangeTypes.FANOUT)
    ))
    public void onSubjectSync(SubjectSyncMessage message) {
        log.debug("Received SubjectSyncMessage: type={}, id={}, deleted={}",
                message.subjectType(), message.subjectId(), message.deleted());

        // ACL Mapping
        AuthzSubjectSyncEvent event = new AuthzSubjectSyncEvent(
                message.subjectType(),
                message.subjectId(),
                message.subjectName(),
                message.displayName(),
                message.email(),
                message.description(),
                message.status(),
                message.version(),
                message.deleted()
        );

        // Handoff to the framework
        subjectManagementService.apply(event);
    }
}
```

---

## 4. Configure RabbitMQ & Jackson

You must register a `Jackson2JsonMessageConverter` so Spring AMQP knows to deserialize the incoming JSON messages.

```java
package org.datamate.your_service.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
```

Add connection credentials to `application.yml`:

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

---

## 5. Implement Contract Testing (Pact)

To ensure the Identity Service (Provider) never breaks your service (Consumer) with silent schema drift, you **must** implement a Pact consumer test. This test verifies that the JSON wire format aligns with your `SubjectSyncMessage` mapping.

Refer to the Pharmacy Service's `SubjectSyncContractTest.java` as a standard template. This test generates a Pact file that the CI pipeline uses to verify the Identity Service.

---

## Architecture Note
The `authz-core` framework strictly adheres to Domain-Driven Design (DDD) and Port/Adapter patterns. The framework provides the *capability* to manage subjects (`SubjectManagementService`), but it is deliberately unaware of RabbitMQ. By enforcing the Anti-Corruption Layer, consumers remain decoupled from the Identity Service's internal packages, and rely strictly on the JSON wire contract verified by Pact.
