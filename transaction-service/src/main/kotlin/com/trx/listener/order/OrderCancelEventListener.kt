package com.trx.listener.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.coroutine.boundedElasticScope
import com.trx.domain.repository.OrderRepository
import com.trx.topic.Topic.ORDER_CANCELED
import com.trx.topic.event.OrderCancelEvent
import com.trx.transaction.OrderSagaInMemoryRepository
import com.trx.transaction.state.OrderCanceled
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * 에러 => 주문 취소 상태로 변경
 *
 * 편의상 도메인 객체 주입.. (OrderRepository)
 */
@Component
class OrderCancelEventListener(
    private val objectMapper: ObjectMapper,
    private val orderRepository: OrderRepository
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [ORDER_CANCELED], groupId = "transaction-orchestrator", containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), OrderCancelEvent::class.java)

        boundedElasticScope.launch {
            OrderSagaInMemoryRepository.findByID(key)?.let {
                it.changeStateAndOperate(
                    OrderCanceled(orderRepository)
                )

                OrderSagaInMemoryRepository.deleteById(key)
            }
        }
    }
}
