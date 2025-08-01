package test.ice
package adapter

import core.model.{ArtistId, Payment}
import core.port.PaymentRepository

import scala.collection.mutable

class InMemoryPaymentRepository extends PaymentRepository {

  private val store = mutable.ListBuffer.empty[Payment]

  override def save(payment: Payment): Unit =
    store += payment

  override def all(): List[Payment] = store.toList
}
