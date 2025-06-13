package no.roar.kafka.retry.listener

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import no.roar.kafka.retry.FOO_TOPIC
import no.roar.kafka.retry.MAX_SEND_COUNT
import no.roar.kafka.retry.TX_PROFILE_NAME
import no.roar.kafka.retry.model.Foo
import no.roar.kafka.retry.service.ConsumerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


@DirtiesContext
abstract class ListenerTestBase : StringSpec() {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<Int, Foo>

    @MockkBean(relaxed = true)
    lateinit var mockConsumerService: ConsumerService

    init {
        "given valid payload when sending message then message should be received by both listeners" {
            publishFooMessage()

            eventually(10.seconds) {
                verify(exactly = 0) { mockConsumerService.handleDltMessage(any()) }
                verify(exactly = 1) {
                    mockConsumerService.handleMessage(firstNameInTest)
                    mockConsumerService.handleReply(firstNameInTest)
                }
            }
        }

        "given valid message and 2 exceptions when processing message then expect call to handleReply" {
            every {
                mockConsumerService.handleMessage(any())
            } throwsMany (List(MAX_SEND_COUNT - 1) {
                RuntimeException("Some error")
            }) andThen Unit

            publishFooMessage()

            eventually(10.seconds) {
                verify(exactly = 0) { mockConsumerService.handleDltMessage(any()) }
                verify(exactly = MAX_SEND_COUNT) { mockConsumerService.handleMessage(firstNameInTest) }
                verify { mockConsumerService.handleReply(any()) }
            }
        }

        "given valid message and 3 exceptions thrown when processing message then expect call to handleDltMessage" {
            every {
                mockConsumerService.handleMessage(any())
            } throwsMany (List(MAX_SEND_COUNT) {
                RuntimeException("Some error")
            })

            publishFooMessage()

            eventually(10.seconds) {
                verify(exactly = 1) { mockConsumerService.handleDltMessage(any()) }
                verify(exactly = MAX_SEND_COUNT) { mockConsumerService.handleMessage(firstNameInTest) }
                verify(exactly = 0) { mockConsumerService.handleReply(any()) }
            }
        }
    }

    private fun publishFooMessage() {
        if (applicationContext.environment.activeProfiles.contains(TX_PROFILE_NAME))
            kafkaTemplate.executeInTransaction { it.send(FOO_TOPIC, MESSAGE_KEY_IN_TEST, fooInTest) }
        else
            kafkaTemplate.send(FOO_TOPIC, MESSAGE_KEY_IN_TEST, fooInTest)
    }

    companion object {
        const val MESSAGE_KEY_IN_TEST = 42
        val firstNameInTest = UUID.randomUUID().toString()
        val fooInTest = Foo(firstNameInTest, "~lastName~")
    }
}