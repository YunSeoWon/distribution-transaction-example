package com.trx.infrastructure.configuration

import com.trx.presentation.listener.order.*
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
    fun orderCreationEventListenerContainerFactory(orderCreationEventListener: OrderCreationEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(orderCreationEventListener)
    }

    @Bean
    fun orderPaymentFailedEventListenerContainerFactory(orderPaymentFailedEventListener: OrderPaymentFailedEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(orderPaymentFailedEventListener)
    }

    @Bean
    fun orderPaymentCompletedEventListenerContainerFactory(orderPaymentCompletedEventListener: OrderPaymentCompletedEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(orderPaymentCompletedEventListener)
    }

    @Bean
    fun orderProductCheckCompletedEventListenerContainerFactory(orderProductCheckCompletedEventListener: OrderProductCheckCompletedEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(orderProductCheckCompletedEventListener)
    }

    @Bean
    fun orderProductCheckFailedEventListenerContainerFactory(orderProductCheckFailedEventListener: OrderProductCheckFailedEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(orderProductCheckFailedEventListener)
    }

    @Bean
    fun orderProductCheckFailedEventListenerContainerFactory(orderRollBackedEventListener: OrderRollBackedEventListener): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory(orderRollBackedEventListener)
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