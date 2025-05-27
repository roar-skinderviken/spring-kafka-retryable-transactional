package no.roar.kafka.retry.listener

import no.roar.kafka.retry.BAR_TOPIC
import no.roar.kafka.retry.FOO_TOPIC
import no.roar.kafka.retry.FOO_TOPIC_DLT
import no.roar.kafka.retry.TX_PROFILE_NAME
import no.roar.kafka.retry.config.ListenerContainerFactoryConfig
import no.roar.kafka.retry.service.ConsumerService
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig.DEFAULT_ISOLATION_LEVEL
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.support.ProducerListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.transaction.KafkaTransactionManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig


@ActiveProfiles(TX_PROFILE_NAME)
@SpringJUnitConfig(
    classes = [
        TransactionalFooListener::class, ConsumerService::class,
        ListenerContainerFactoryConfig::class, TransactionalFooListenerSpringJUnitTest.KafkaConfig::class
    ]
)
@EmbeddedKafka
class TransactionalFooListenerSpringJUnitTest : ListenerTestBase() {

    @TestConfiguration
    class KafkaConfig(
        @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String
    ) {
        @Bean
        fun topics(): KafkaAdmin.NewTopics = KafkaAdmin.NewTopics(
            TopicBuilder.name(FOO_TOPIC)
                .build(),
            TopicBuilder.name(FOO_TOPIC_DLT)
                .build(),
            TopicBuilder.name(BAR_TOPIC)
                .build()
        )

        @Bean
        fun producerFactory(): ProducerFactory<Any, Any> = DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to IntegerSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                ProducerConfig.TRANSACTIONAL_ID_CONFIG to "tx."
            )
        )

        @Bean
        fun kafkaTemplate(
            producerFactory: ProducerFactory<Any, Any>
        ): KafkaTemplate<*, *> = KafkaTemplate(producerFactory).apply {
            transactionIdPrefix = "tx."
            setProducerListener(object : ProducerListener<Any, Any> {
                override fun onSuccess(producerRecord: ProducerRecord<Any, Any>, recordMetadata: RecordMetadata) {
                    println(
                        "Message sent successfully: " +
                                "Topic = " + producerRecord.topic() +
                                ", Key = " + producerRecord.key() +
                                ", Value = " + producerRecord.value() +
                                ", Partition = " + recordMetadata.partition() +
                                ", Offset = " + recordMetadata.offset()
                    )
                }

                override fun onError(
                    producerRecord: ProducerRecord<Any, Any>,
                    recordMetadata: RecordMetadata?,
                    exception: Exception
                ) {
                    System.err.println(
                        ("Error sending message: " +
                                "Topic = " + producerRecord.topic() +
                                ", Key = " + producerRecord.key() +
                                ", Value = " + producerRecord.value() +
                                ", Error = " + exception.message)
                    )
                }
            })
        }

        @Bean
        fun consumerFactory(): ConsumerFactory<Any, Any> = DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to IntegerDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
                ConsumerConfig.ISOLATION_LEVEL_CONFIG to DEFAULT_ISOLATION_LEVEL,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                JsonDeserializer.TYPE_MAPPINGS to "foo:no.roar.kafka.retry.model.Foo,bar:no.roar.kafka.retry.model.Bar"
            )
        )

        @Bean
        fun kafkaTransactionManager(
            producerFactory: ProducerFactory<Any, Any>
        ): KafkaTransactionManager<Any, Any> = KafkaTransactionManager(producerFactory)
    }
}