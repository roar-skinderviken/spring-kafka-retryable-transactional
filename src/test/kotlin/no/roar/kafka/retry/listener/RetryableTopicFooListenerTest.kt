package no.roar.kafka.retry.listener

import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import io.mockk.verify
import no.roar.kafka.retry.FOO_TOPIC
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.MESSAGE_KEY_IN_TEST
import no.roar.kafka.retry.listener.ListenerTestBase.Companion.fooInTest
import no.roar.kafka.retry.model.Foo
import no.roar.kafka.retry.service.ConsumerService
import org.apache.kafka.clients.consumer.ConsumerRecord

class RetryableTopicFooListenerTest : StringSpec({
    val consumerService: ConsumerService = mockk(relaxed = true)
    val sut = RetryableTopicFooListener(consumerService)

    "given valid message when calling listenForFoo expect handleMessage to be called" {
        sut.listenForFoo(fooRecordInTest.value(), 42)

        verify { consumerService.handleMessage(any()) }
    }
}) {
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