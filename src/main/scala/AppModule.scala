package test.ice

import adapter._
import core.service._
import core.port._

object AppModule {
  // Adapters (in-memory infrastructure)
  val artistRepo: ArtistRepository = new InMemoryArtistRepository()
  val releaseRepo: ReleaseRepository = new InMemoryReleaseRepository()
  val streamRepo: StreamRepository = new InMemoryStreamRepository()
  val paymentRepo: PaymentRepository = new InMemoryPaymentRepository()

  // Core services
  val releaseService: ReleaseService = new ReleaseService(releaseRepo, artistRepo)
  val streamingService: StreamingService = new StreamingService(releaseRepo, streamRepo)
  val reportService: StreamingReportService = new StreamingReportService(streamRepo, releaseRepo)
  val paymentService: PaymentService = new PaymentService(streamRepo, paymentRepo)
  val searchService: ReleaseSearchService = new ReleaseSearchService(releaseRepo)
}
