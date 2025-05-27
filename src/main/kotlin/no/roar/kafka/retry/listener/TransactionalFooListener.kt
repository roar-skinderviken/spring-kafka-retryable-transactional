package no.roar.kafka.retry.listener

import no.roar.kafka.retry.model.Bar
import no.roar.kafka.retry.model.Foo
import no.roar.kafka.retry.*
import no.roar.kafka.retry.service.ConsumerService
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
@Profile(TX_PROFILE_NAME)
class TransactionalFooListener(
    private val consumerService: ConsumerService
) {
    @Transactional
    @KafkaListener(
        id = "fooListener",
        topics = [FOO_TOPIC]
    )
    @SendTo(BAR_TOPIC)
    fun listenForFoo(
        payload: Foo,
        @Header(KafkaHeaders.RECEIVED_KEY) key: Int
    ): Bar {
        log.info("listenForFoo received: {} with key: {}", payload, key)
        consumerService.handleMessage(payload.firstName)
        return Bar(payload.firstName, payload.lastName)
    }

    @KafkaListener(
        id = "replyListener",
        topics = [BAR_TOPIC],
    )
    fun listenForReply(
        payload: Bar,
        @Header(KafkaHeaders.RECEIVED_KEY) key: Int
    ) {
        log.info("listenForReply received: {} with key: {}", payload, key)
        consumerService.handleReply(payload.firstName)
    }

    @KafkaListener(
        id = "dltListener",
        topics = [FOO_TOPIC_DLT],
    )
    fun handleFooDlt(
        message: Foo,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String
    ) {
        log.info("Event on dlt topic={}, payload={}", topic, message)
        consumerService.handleDltMessage(message.firstName)
    }

    companion object {
        private val log = loggerFor<TransactionalFooListener>()
    }
}