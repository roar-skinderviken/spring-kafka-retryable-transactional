package no.roar.kafka.retry.service

import no.roar.kafka.retry.loggerFor
import org.springframework.stereotype.Service

@Service
class ConsumerService {

    fun handleMessage(payload: String) = log.info("handleMessage received: {}", payload)

    fun handleReply(payload: String) = log.info("handleReply received: {}", payload)

    fun handleDltMessage(payload: String) = log.info("handleDltMessage received: {}", payload)

    companion object {
        private val log = loggerFor<ConsumerService>()
    }
}