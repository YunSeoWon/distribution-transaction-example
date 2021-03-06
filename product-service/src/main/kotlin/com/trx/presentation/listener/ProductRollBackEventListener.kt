package com.trx.presentation.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import com.trx.application.product.ProductCommandService
import com.trx.coroutine.boundedElasticScope
import com.trx.topic.Topic
import com.trx.topic.Topic.PRODUCT_ROLLBACK
import com.trx.topic.event.OrderRollBacked
import com.trx.topic.event.ProductRollBackEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ProductRollBackEventListener(
    private val objectMapper: ObjectMapper,
    private val productCommandService: ProductCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [PRODUCT_ROLLBACK], groupId = "product-consumer", containerFactory = "productRollBackEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = data.key() to objectMapper.readValue(data.value(), ProductRollBackEvent::class.java)

        logger.info("Topic: $PRODUCT_ROLLBACK, key: $key, event: $event")

        productCommandService.incrementProductCount(event)

        boundedElasticScope.launch {
            transactionEventPublisher.publishEvent(
                topic = Topic.ORDER_ROLLBACKED,
                key = key,
                event = OrderRollBacked(event.failueReason)
            ).awaitFirstOrNull()
        }

        acknowledgment.acknowledge()
    }
}
