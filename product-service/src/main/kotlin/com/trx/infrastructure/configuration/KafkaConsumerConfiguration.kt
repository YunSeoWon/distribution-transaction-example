package com.trx.infrastructure.configuration

import com.trx.presentation.listener.ProductEventListener
import com.trx.presentation.listener.ProductRollBackEventListener
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener

@Configuration
class KafkaConsumerConfiguration(
    private val kafkaProperties: KafkaProperties
) {

    @Bean
    fun productEventListenerContainerFactory(productEventListener: ProductEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(productEventListener)
    }

    @Bean
    fun productRollBackEventListenerContainerFactory(productRollBackEventListener: ProductRollBackEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(productRollBackEventListener)
    }

    private fun makeFactory(listener: AcknowledgingMessageListener<String, String>): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            consumerFactory = consumerFactory()
            containerProperties.ackMode = kafkaProperties.listener.ackMode
            setConcurrency(3)
            containerProperties.messageListener = listener
            containerProperties.pollTimeout = 5000
        }
    }

    private fun consumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties())
    }
}