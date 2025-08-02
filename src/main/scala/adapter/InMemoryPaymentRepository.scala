package test.ice
package adapter

import core.model.Payment
import core.port.PaymentRepository

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters.*

class InMemoryPaymentRepository extends PaymentRepository {

  private val store = new ConcurrentLinkedQueue[Payment]()

  override def save(payment: Payment): Unit =
    store.add(payment)

  override def all(): List[Payment] = store.iterator().asScala.toList
}
