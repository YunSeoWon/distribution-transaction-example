package com.trx.presentation.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.order.OrderCommandService
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic.ORDER_COMPLETED
import com.trx.topic.event.OrderCompleted
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 성공 => 주문 승인 상태로 변경 (FINAL)
 */
@Component
class OrderCompletedEventListener(
    private val objectMapper: ObjectMapper,
    private val orderCommandService: OrderCommandService
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_COMPLETED], groupId = "order-consumer", containerFactory = "orderCompletedEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCompleted::class.java)

        logger.info("Topic: $ORDER_COMPLETED, key: $key, event: $event")

        boundedElasticScope.launch {
            orderCommandService.approve(event.orderId)
        }

        acknowledgment.acknowledge()
    }
}
