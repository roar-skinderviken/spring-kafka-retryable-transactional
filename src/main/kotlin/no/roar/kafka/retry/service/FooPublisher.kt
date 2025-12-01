package no.roar.kafka.retry.service

import no.roar.kafka.retry.FOO_TOPIC
import no.roar.kafka.retry.loggerFor
import no.roar.kafka.retry.model.Foo
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class FooPublisher(
    private val kafkaTemplate: KafkaTemplate<Int, Foo>
) {
    @Transactional
    fun publishFoo(key: Int, message: Foo) {
        kafkaTemplate.send(FOO_TOPIC, key, message)
            .whenComplete { sendResult, throwable ->
                if (throwable != null) {
                    log.warn("Send failed", throwable)
                } else {
                    log.info(
                        "Send succeeded: {} at offset {}",
                        sendResult.recordMetadata.topic(),
                        sendResult.recordMetadata.offset()
                    )
                }
            }
    }

    companion object {
        private val log = loggerFor<FooPublisher>()
    }
}