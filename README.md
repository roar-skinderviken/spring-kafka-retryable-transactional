# spring-kafka-retryable-transactional
A Spring Kafka application demonstrating how to migrate from the @RetryableTopic 
annotation to a transactional retry mechanism.

# Overview
The starting point is the
[RetryableTopicFooListener](src/main/kotlin/no/roar/kafka/retry/listener/RetryableTopicFooListener.kt)
which is not using transactions. 
Tests for this class are implemented in
[RetryableTopicFooListenerIntegrationTest](src/test/kotlin/no/roar/kafka/retry/listener/RetryableTopicFooListenerIntegrationTest.kt).

The transactional version is implemented in 
[TransactionalFooListener](src/main/kotlin/no/roar/kafka/retry/listener/TransactionalFooListener.kt) and is activated by
profile = "tx". Tests for this class are implemented in
[TransactionalFooListenerContainerTest](src/test/kotlin/no/roar/kafka/retry/listener/TransactionalFooListenerIntegrationTest.kt).

[TransactionalFooListenerContainerTest](src/test/kotlin/no/roar/kafka/retry/listener/TransactionalFooListenerContainerTest.kt)
and
[TransactionalFooListenerSpringJUnitTest](src/test/kotlin/no/roar/kafka/retry/listener/TransactionalFooListenerSpringJUnitTest.kt)
show the same transaction tests using ConfluentKafkaContainer and @SpringJUnitConfig respectively.

The `ConcurrentKafkaListenerContainerFactory` is configured with a `DefaultAfterRollbackProcessor` in 
[ListenerContainerFactoryConfig](src/main/kotlin/no/roar/kafka/retry/config/ListenerContainerFactoryConfig.kt)

An alternative approach using `BeanPostProcessor` is in 
[ListenerContainerFactoryBeanPostProcessor](src/main/kotlin/no/roar/kafka/retry/config/ListenerContainerFactoryBeanPostProcessor.kt).

@SpringBoot- and @SpringJUnitConfig tests are sharing the same tests defined in
[ListenerTestBase](src/test/kotlin/no/roar/kafka/retry/listener/ListenerTestBase.kt)

# Prerequisites
Java 21+
Docker (for running test container tests)

# Running Test
```shell
./gradlew clean check --info
```
