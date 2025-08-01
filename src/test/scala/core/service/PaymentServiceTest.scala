package test.ice
package core.service

import org.scalatest.funsuite.AnyFunSuite
import core.model.*
import core.service.PaymentService
import adapter.{InMemoryPaymentRepository, InMemoryStreamRepository}

import test.ice.core.error.AppError

import java.time.Instant

class PaymentServiceTest extends AnyFunSuite {

  test("should create payment for all monetized streams (first time)") {
    val streamRepo = new InMemoryStreamRepository()
    val paymentRepo = new InMemoryPaymentRepository()
    val paymentService = new PaymentService(streamRepo, paymentRepo)

    val artistId = ArtistId("a1")
    val releaseId = ReleaseId("r1")

    val now = Instant.now()
    streamRepo.save(Stream(SongId("s1"), releaseId, artistId, 40, now, monetized = true))
    streamRepo.save(Stream(SongId("s2"), releaseId, artistId, 60, now, monetized = true))

    val result = paymentService.fileForPayment(artistId)

    assert(result.isRight)
    val payment = result.toOption.get
    assert(payment.artistId == artistId)
    assert(payment.totalStreams == 2)
    assert(payment.totalAmount == BigDecimal("0.02"))
  }

  test("should create payment only for streams after last payment") {
    val streamRepo = new InMemoryStreamRepository()
    val paymentRepo = new InMemoryPaymentRepository()
    val paymentService = new PaymentService(streamRepo, paymentRepo)

    val artistId = ArtistId("a2")
    val releaseId = ReleaseId("r2")

    val oldTime = Instant.now().minusSeconds(3600)
    val recentTime = Instant.now()

    // Stream before first payment
    streamRepo.save(Stream(SongId("s1"), releaseId, artistId, 50, oldTime, monetized = true))
    val previousPayment = Payment(artistId, oldTime.plusSeconds(10), 1, BigDecimal("0.01"))
    paymentRepo.save(previousPayment)

    // New eligible stream
    streamRepo.save(Stream(SongId("s2"), releaseId, artistId, 45, recentTime, monetized = true))

    val result = paymentService.fileForPayment(artistId)

    assert(result.isRight)
    val payment = result.toOption.get
    assert(payment.totalStreams == 1)
    assert(payment.totalAmount == BigDecimal("0.01"))
  }

  test("should return error if no new monetized streams since last payment") {
    val streamRepo = new InMemoryStreamRepository()
    val paymentRepo = new InMemoryPaymentRepository()
    val paymentService = new PaymentService(streamRepo, paymentRepo)

    val artistId = ArtistId("a3")
    val releaseId = ReleaseId("r3")
    val now = Instant.now()

    // Monetized stream before payment
    streamRepo.save(Stream(SongId("s1"), releaseId, artistId, 50, now.minusSeconds(60), monetized = true))
    paymentRepo.save(Payment(artistId, now, 1, BigDecimal("0.01")))

    val result = paymentService.fileForPayment(artistId)

    assert(result.isLeft)
    assert(result == Left(AppError.NotFound))
  }
}
