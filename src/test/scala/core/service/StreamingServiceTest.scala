package test.ice
package core.service

import adapter.{InMemoryReleaseRepository, InMemoryStreamRepository}
import core.error.AppError
import core.model.*

import org.scalatest.funsuite.AnyFunSuite

class StreamingServiceTest extends AnyFunSuite {

  test("should record monetized stream for released song with duration >= 30") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()
    val streamingService = new StreamingService(releaseRepo, streamRepo)

    val song = Song(SongId("s1"), "Streamable Track", 200)
    val release = Release(
      id = ReleaseId("r1"),
      title = "Released Album",
      artistId = ArtistId("a1"),
      songs = List(song),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    releaseRepo.save(release)

    val result = streamingService.recordStream(song.id, 45)

    assert(result.isRight)
    val recorded = streamingService.bySong(song.id)
    assert(recorded.size == 1)
    assert(recorded.head.monetized)
  }

  test("should record non-monetized stream if duration < 30") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()
    val streamingService = new StreamingService(releaseRepo, streamRepo)

    val song = Song(SongId("s2"), "Short Play", 120)
    val release = Release(
      id = ReleaseId("r2"),
      title = "Released Album",
      artistId = ArtistId("a2"),
      songs = List(song),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    releaseRepo.save(release)

    val result = streamingService.recordStream(song.id, 20)

    assert(result.isRight)
    val recorded = streamingService.bySong(song.id)
    assert(!recorded.head.monetized)
  }

  test("should return error if release is not released") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()
    val streamingService = new StreamingService(releaseRepo, streamRepo)

    val song = Song(SongId("s3"), "Unreleased Track", 180)
    val release = Release(
      id = ReleaseId("r3"),
      title = "Scheduled Album",
      artistId = ArtistId("a3"),
      songs = List(song),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Scheduled
    )
    releaseRepo.save(release)

    val result = streamingService.recordStream(song.id, 40)

    assert(result == Left(AppError("Release is not active for streaming")))
  }

  test("should return error if release is taken down") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()
    val streamingService = new StreamingService(releaseRepo, streamRepo)

    val song = Song(SongId("s4"), "Removed Track", 190)
    val release = Release(
      id = ReleaseId("r4"),
      title = "Withdrawn Album",
      artistId = ArtistId("a4"),
      songs = List(song),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.TakenDown
    )
    releaseRepo.save(release)

    val result = streamingService.recordStream(song.id, 40)

    assert(result == Left(AppError("This release has been taken down")))
  }

  test("should return error if song is not found in any release") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()
    val streamingService = new StreamingService(releaseRepo, streamRepo)

    val result = streamingService.recordStream(SongId("unknown"), 45)

    assert(result == Left(AppError("Song not found in any release")))
  }

  test("should return error if duration is zero or negative") {
    val releaseRepo = new InMemoryReleaseRepository()
    val streamRepo = new InMemoryStreamRepository()
    val streamingService = new StreamingService(releaseRepo, streamRepo)

    val result = streamingService.recordStream(SongId("any"), 0)

    assert(result == Left(AppError("Duration must be positive")))
  }
}

