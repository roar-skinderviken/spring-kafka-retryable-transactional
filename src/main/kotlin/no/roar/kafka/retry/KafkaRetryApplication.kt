package no.roar.kafka.retry

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


inline fun <reified T : Any> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)

const val TX_PROFILE_NAME = "tx"
const val FOO_TOPIC = "foo-topic"
const val FOO_TOPIC_DLT = "foo-topic-dlt"
const val BAR_TOPIC = "bar-topic"
const val MAX_SEND_COUNT = 3

@SpringBootApplication
class KafkaRetryApplication

fun main(args: Array<String>) {
    runApplication<KafkaRetryApplication>(*args)
}
