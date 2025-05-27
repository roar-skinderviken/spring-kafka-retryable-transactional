package no.roar.kafka.retry.listener

import no.roar.kafka.retry.TX_PROFILE_NAME
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(TX_PROFILE_NAME)
@SpringBootTest
@EmbeddedKafka
class TransactionalFooListenerIntegrationTest : ListenerTestBase()