package no.roar.kafka.retry.listener

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka

@SpringBootTest
@EmbeddedKafka
class RetryableTopicFooListenerIntegrationTest: ListenerTestBase()