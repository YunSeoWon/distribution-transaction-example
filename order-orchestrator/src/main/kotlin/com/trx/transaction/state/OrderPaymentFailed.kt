package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.*
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * -> ORDER_FAILED (FINISHED)
 */
class OrderPaymentFailed(
    val failureReason: String
): OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.PRODUCT_ROLLBACK,
            saga.key,
            ProductRollBackEvent(saga.productId, saga.count, failureReason)
        ).awaitSingle()
    }
}