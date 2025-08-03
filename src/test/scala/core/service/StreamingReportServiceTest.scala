package test.ice
package core.service

import adapter.{InMemoryReleaseRepository, InMemoryStreamRepository}
import core.model.*

import org.scalatest.funsuite.AnyFunSuite

import java.time.Instant

class StreamingReportServiceTest extends AnyFunSuite {

  test("success: should generate correct report with mixed streams") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()

    val song1 = Song(SongId("s1"), "Track One", 180)
    val song2 = Song(SongId("s2"), "Track Two", 200)

    val release = Release(
      id = ReleaseId("r1"),
      title = "Sample Release",
      artistId = ArtistId("a1"),
      songs = List(song1, song2),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    releaseRepo.save(release)

    val now = Instant.now()
    streamRepo.save(Stream(song1.id, release.id, release.artistId, 40, now, monetized = true))
    streamRepo.save(Stream(song1.id, release.id, release.artistId, 20, now, monetized = false))
    streamRepo.save(Stream(song1.id, release.id, release.artistId, 33, now, monetized = true))
    streamRepo.save(Stream(song2.id, release.id, release.artistId, 25, now, monetized = false))

    val service = new StreamingReportService(streamRepo, releaseRepo)
    val report = service.generateReport(ArtistId("a1"))

    val entry1 = report.find(_.songId == song1.id).get
    val entry2 = report.find(_.songId == song2.id).get

    assert(entry1.monetizedCount == 2)
    assert(entry1.nonMonetizedCount == 1)
    assert(entry1.title == "Track One")

    assert(entry2.monetizedCount == 0)
    assert(entry2.nonMonetizedCount == 1)
    assert(entry2.title == "Track Two")
  }

  test("fail: should return empty list if artist has no streams") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()

    val service = new StreamingReportService(streamRepo, releaseRepo)
    val report = service.generateReport(ArtistId("unknown"))

    assert(report.isEmpty)
  }
}
