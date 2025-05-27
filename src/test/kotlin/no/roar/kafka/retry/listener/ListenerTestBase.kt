package no.roar.kafka.retry.listener

import no.roar.kafka.retry.FOO_TOPIC
import no.roar.kafka.retry.MAX_SEND_COUNT
import no.roar.kafka.retry.TX_PROFILE_NAME
import no.roar.kafka.retry.model.Foo
import no.roar.kafka.retry.service.ConsumerService
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID
import java.util.concurrent.TimeUnit


@DirtiesContext
abstract class ListenerTestBase {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<Int, Foo>

    @MockitoBean
    lateinit var consumerService: ConsumerService

    init {
        Awaitility.setDefaultTimeout(10, TimeUnit.SECONDS)
    }

    private val publishFooMessageFunc: () -> Unit by lazy {
        if (applicationContext.environment.activeProfiles.contains(TX_PROFILE_NAME)) txPublishFunc
        else defaultPublishFunc
    }

    @Test
    fun `given valid payload when sending message then message should be received by both listeners`() {
        doNothing().`when`(consumerService).handleMessage(anyString())

        publishFooMessageFunc()

        await().untilAsserted {
            verify(consumerService, never()).handleDltMessage(anyString())
            verify(consumerService).handleMessage(firstNameInTest)
            verify(consumerService).handleReply(firstNameInTest)
        }
    }

    @Test
    fun `given valid message and 2 exceptions when calling send then expect call to handleReply`() {
        doThrow(*Array(MAX_SEND_COUNT - 1) { RuntimeException("Some error") })
            .doNothing()
            .`when`(consumerService).handleMessage(anyString())

        publishFooMessageFunc()

        await().untilAsserted {
            verify(consumerService, never()).handleDltMessage(anyString())
            verify(consumerService, times(MAX_SEND_COUNT)).handleMessage(firstNameInTest)
            verify(consumerService).handleReply(anyString())
        }
    }

    @Test
    fun `given valid message and 3 exceptions thrown when calling send then expect call to handleDltMessage`() {
        doThrow(*Array(MAX_SEND_COUNT) { RuntimeException("Some error") })
            .`when`(consumerService).handleMessage(anyString())

        publishFooMessageFunc()

        await().untilAsserted {
            verify(consumerService, never()).handleReply(anyString())
            verify(consumerService).handleDltMessage(anyString())
            verify(consumerService, times(MAX_SEND_COUNT)).handleMessage(firstNameInTest)
        }
    }

    private val defaultPublishFunc: () -> Unit = {
        kafkaTemplate.send(FOO_TOPIC, MESSAGE_KEY_IN_TEST, fooInTest)
    }

    private val txPublishFunc: () -> Unit = {
        kafkaTemplate.executeInTransaction {
            it.send(FOO_TOPIC, MESSAGE_KEY_IN_TEST, fooInTest)
        }
    }

    companion object {
        const val MESSAGE_KEY_IN_TEST = 42
        val firstNameInTest = UUID.randomUUID().toString()
        val fooInTest = Foo(firstNameInTest, "~lastName~")
    }
}