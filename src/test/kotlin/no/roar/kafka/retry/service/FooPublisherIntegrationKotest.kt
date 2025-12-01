package no.roar.kafka.retry.service

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.StringSpec
import io.mockk.verify
import no.roar.kafka.retry.TX_PROFILE_NAME
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.MESSAGE_KEY_IN_TEST
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.fooInTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import kotlin.time.Duration.Companion.seconds

@ActiveProfiles(TX_PROFILE_NAME)
@SpringBootTest
@EmbeddedKafka
@DirtiesContext
class FooPublisherIntegrationKotest(
    samplePublisher: FooPublisher,
    @MockkBean(relaxed = true) private val mockConsumerService: ConsumerService
) : StringSpec({

    "given a valid Foo when calling publishFoo expect handleMessage and handleReply to be called" {
        samplePublisher.publishFoo(MESSAGE_KEY_IN_TEST, fooInTest)

        eventually(20.seconds) {
            verify { mockConsumerService.handleMessage(fooInTest.firstName) }
            verify { mockConsumerService.handleReply(fooInTest.firstName) }
        }
    }
})