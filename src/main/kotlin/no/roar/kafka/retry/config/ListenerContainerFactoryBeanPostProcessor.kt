package no.roar.kafka.retry.config

import no.roar.kafka.retry.MAX_SEND_COUNT
import no.roar.kafka.retry.TX_PROFILE_NAME
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Profile
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries

@Profile(TX_PROFILE_NAME)
//@Component
class ListenerContainerFactoryBeanPostProcessor(
    private val kafkaTemplate: KafkaTemplate<Any, Any>
) : BeanPostProcessor {
    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String
    ): Any = bean.also { currentBean ->
        if (currentBean is ConcurrentKafkaListenerContainerFactory<*, *>) {
            currentBean.setAfterRollbackProcessor(
                DefaultAfterRollbackProcessor(
                    DeadLetterPublishingRecoverer(kafkaTemplate),
                    ExponentialBackOffWithMaxRetries(MAX_SEND_COUNT - 1).apply {
                        initialInterval = 100L // ms
                        maxInterval = 2_000L // ms
                        multiplier = 1.4
                    }
                )
            )
        }
    }
}