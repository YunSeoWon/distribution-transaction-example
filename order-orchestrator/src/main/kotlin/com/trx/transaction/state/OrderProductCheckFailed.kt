package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.OrderCancelEvent
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * -> ORDER_CANCELED (FINISHED)
 */
class OrderProductCheckFailed(
    val failureReason: String
): OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.ORDER_CANCELED,
            saga.key,
            OrderCancelEvent(saga.orderId, failureReason)
        ).awaitSingle()
    }
}