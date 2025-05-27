package no.roar.kafka.retry.service

import no.roar.kafka.retry.TX_PROFILE_NAME
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.MESSAGE_KEY_IN_TEST
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.fooInTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles(TX_PROFILE_NAME)
@SpringBootTest
@EmbeddedKafka
@DirtiesContext
class FooPublisherIntegrationTest {

    @Autowired
    lateinit var samplePublisher: FooPublisher

    @MockitoBean
    lateinit var consumerService: ConsumerService

    @Test
    fun `given valid Foo when calling publishFoo expect handleMessage and handleReply to be called`() {
        samplePublisher.publishFoo(MESSAGE_KEY_IN_TEST, fooInTest)

        await().untilAsserted {
            verify(consumerService).handleMessage(fooInTest.firstName)
            verify(consumerService).handleReply(fooInTest.firstName)
        }
    }
}