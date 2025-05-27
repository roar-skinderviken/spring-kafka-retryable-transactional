package no.roar.kafka.retry.listener

import no.roar.kafka.retry.FOO_TOPIC
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.MESSAGE_KEY_IN_TEST
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.fooInTest
import no.roar.kafka.retry.model.Foo
import no.roar.kafka.retry.service.ConsumerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RetryableTopicFooListenerTest {

    @Mock
    lateinit var consumerService: ConsumerService

    @InjectMocks
    lateinit var sampleListener: RetryableTopicFooListener

    @Test
    fun `given valid message when calling listenForFoo expect handleMessage to be called`() {
        sampleListener.listenForFoo(fooRecordInTest.value(), 42)

        verify(consumerService).handleMessage(anyString())
    }

    companion object {
        val fooRecordInTest: ConsumerRecord<Int, Foo> = ConsumerRecord(
            FOO_TOPIC,
            0,
            0,
            MESSAGE_KEY_IN_TEST,
            fooInTest
        )
    }
}