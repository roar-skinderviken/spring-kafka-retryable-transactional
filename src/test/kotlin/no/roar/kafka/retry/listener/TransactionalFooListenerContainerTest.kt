package no.roar.kafka.retry.listener

import no.roar.kafka.retry.TX_PROFILE_NAME
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.kafka.ConfluentKafkaContainer

@ActiveProfiles(TX_PROFILE_NAME)
@SpringBootTest
class TransactionalFooListenerContainerTest : ListenerTestBase() {

    companion object {

        @ServiceConnection
        var kafka = ConfluentKafkaContainer("confluentinc/cp-kafka:7.8.0").apply {
            start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) =
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
    }
}