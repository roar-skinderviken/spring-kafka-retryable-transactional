package no.roar.kafka.retry.config

import no.roar.kafka.retry.MAX_SEND_COUNT
import no.roar.kafka.retry.TX_PROFILE_NAME
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.kafka.transaction.KafkaTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement


@Profile(TX_PROFILE_NAME)
@EnableKafka
@EnableTransactionManagement
@Configuration(proxyBeanMethods = false)
class ListenerContainerFactoryConfig {

    @Bean
    fun kafkaListenerContainerFactory(
        kafkaTemplate: KafkaTemplate<Any, Any>,
        consumerFactory: ConsumerFactory<Any, Any>,
        kafkaTransactionManager: KafkaTransactionManager<Any, Any>
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> =
        ConcurrentKafkaListenerContainerFactory<Any, Any>().apply {
            setConsumerFactory(consumerFactory)
            setReplyTemplate(kafkaTemplate)

            containerProperties.kafkaAwareTransactionManager = kafkaTransactionManager
            containerProperties.eosMode = ContainerProperties.EOSMode.V2

            this.setAfterRollbackProcessor(
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