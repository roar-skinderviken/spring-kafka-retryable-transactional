package no.roar.kafka.retry.listener

import no.roar.kafka.retry.TX_PROFILE_NAME
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.kafka.ConfluentKafkaContainer

@ActiveProfiles(TX_PROFILE_NAME)
@SpringBootTest
class TransactionalFooListenerContainerTest : ListenerTestBase() {

    companion object {

        @Suppress("unused")
        @ServiceConnection
        var kafka = ConfluentKafkaContainer("confluentinc/cp-kafka:7.8.0")
    }
}