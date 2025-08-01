package test.ice
package core.service

import core.model._
import core.port.{PaymentRepository, StreamRepository}
import core.error.AppError

import java.time.Instant

class PaymentService(
                      streamRepo: StreamRepository,
                      paymentRepo: PaymentRepository
                    ) {
  private val pricePerStream = BigDecimal("0.01")


   // Calculates and files a payment for the given artist.
   // This method:
   // - Fetches all monetized streams for the artist
   // - Filters streams that occurred after the last recorded payment (if any)
   // - Calculates total amount using a fixed per-stream rate
   // - Stores the new payment and returns it
   // We assume price per stream to be 0.01 Unit Currency
   // If there are no eligible new monetized streams, returns an error.
  def fileForPayment(artistId: ArtistId): Either[AppError, Payment] = {
    val monetizedStreams = streamRepo
      .all()
      .filter(s => s.artistId == artistId && s.monetized)

    val lastPaidAt = paymentRepo
      .all()
      .filter(_.artistId == artistId)
      .map(_.paidAt)
      .sortWith(_ isAfter _)
      .headOption

    val newStreams = lastPaidAt match {
      case Some(lastPaid) => monetizedStreams.filter(_.streamedAt.isAfter(lastPaid))
      case None => monetizedStreams
    }

    if (newStreams.isEmpty)
      return Left(AppError.NotFound)

    val streamCount = newStreams.size
    val totalAmount = pricePerStream * streamCount
    val payment = Payment(artistId, Instant.now(), streamCount, totalAmount)

    paymentRepo.save(payment)
    Right(payment)
  }
}

