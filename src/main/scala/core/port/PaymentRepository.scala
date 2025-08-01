package test.ice
package core.port

import core.model.{ArtistId, Payment}

trait PaymentRepository {
  def save(payment: Payment): Unit
  def all(): List[Payment]
}
