package test.ice
package core.service

import org.scalatest.funsuite.AnyFunSuite
import core.model.*
import core.service.ReleaseSearchService
import adapter.{InMemoryArtistRepository, InMemoryReleaseRepository}

import test.ice.core.port.ArtistRepository

class ReleaseSearchTest extends AnyFunSuite {

  test("should return multiple songs with titles similar to the query") {
    val repo = new InMemoryReleaseRepository()
    val releaseService = new ReleaseService(releaseRepo = repo, artistRepo = InMemoryArtistRepository())
    val searchService = new ReleaseSearchService(repo)

    val release1 = Release(
      id = ReleaseId(""),
      title = "Released Album",
      artistId = ArtistId("a1"),
      songs = List(
        Song(SongId("s1"), "Hullo World", 180), // match
        Song(SongId("s2"), "Hollow World", 160), // match
        Song(SongId("s4"), "Night Runner", 210),
        Song(SongId("s5"), "Ocean Drive", 190),
        Song(SongId("s6"), "Neon Lights", 175),
        Song(SongId("s7"), "Star Chaser", 220),
        Song(SongId("s8"), "Silent Echo", 160),
        Song(SongId("s9"), "Parallel Worlds", 205),
        Song(SongId("s10"), "Virtual Love", 195)
      ),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    val release2 = Release(
      id = ReleaseId(""),
      title = "Released Album",
      artistId = ArtistId("a2"),
      songs = List(
        Song(SongId("s3"), "Hell World", 200), // match
        Song(SongId("s4"), "Night Runner", 210),
        Song(SongId("s5"), "Ocean Drive", 190),
        Song(SongId("s6"), "Neon Lights", 175),
        Song(SongId("s7"), "Star Chaser", 220),
        Song(SongId("s8"), "Silent Echo", 160),
        Song(SongId("s9"), "Parallel Worlds", 205),
        Song(SongId("s10"), "Virtual Love", 195)
      ),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    releaseService.createRelease(release1)
    releaseService.createRelease(release2)

    val result = searchService.searchSimilar("Hello World")

    val matchedTitles = result.map(_.title)

    assert(matchedTitles.contains("Hullo World"))
    assert(matchedTitles.contains("Hollow World"))
    assert(matchedTitles.contains("Hell World"))
    assert(matchedTitles.size == 3)
  }

  test("should not return songs that are not similar enough") {
    val repo = new InMemoryReleaseRepository()
    val releaseService = new ReleaseService(releaseRepo = repo, artistRepo = InMemoryArtistRepository())
    val searchService = new ReleaseSearchService(repo)

    val release1 = Release(
      id = ReleaseId(""),
      title = "Released Album",
      artistId = ArtistId("a1"),
      songs = List(
        Song(SongId("s1"), "Hullo World", 180),
        Song(SongId("s2"), "Hollow World", 160),
        Song(SongId("s4"), "Night Runner", 210),
        Song(SongId("s5"), "Ocean Drive", 190),
        Song(SongId("s6"), "Neon Lights", 175),
        Song(SongId("s7"), "Star Chaser", 220),
        Song(SongId("s8"), "Silent Echo", 160),
        Song(SongId("s9"), "Parallel Worlds", 205),
        Song(SongId("s10"), "Virtual Love", 195)
      ),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    val release2 = Release(
      id = ReleaseId(""),
      title = "Released Album",
      artistId = ArtistId("a2"),
      songs = List(
        Song(SongId("s3"), "Hell World", 200),
        Song(SongId("s4"), "Night Runner", 210),
        Song(SongId("s5"), "Ocean Drive", 190),
        Song(SongId("s6"), "Neon Lights", 175),
        Song(SongId("s7"), "Star Chaser", 220),
        Song(SongId("s8"), "Silent Echo", 160),
        Song(SongId("s9"), "Parallel Worlds", 205),
        Song(SongId("s10"), "Virtual Love", 195)
      ),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Released
    )
    releaseService.createRelease(release1)
    releaseService.createRelease(release2)

    val result = searchService.searchSimilar("Not like us")

    val matchedTitles = result.map(_.title)

    assert(result.isEmpty)
    assert(matchedTitles.isEmpty)
  }

  test("should ignore songs from releases not in Released state") {
    val repo = new InMemoryReleaseRepository()
    val releaseService = new ReleaseService(releaseRepo = repo, artistRepo = InMemoryArtistRepository())
    val searchService = new ReleaseSearchService(repo)

    val release = Release(
      id = ReleaseId("r3"),
      title = "Unreleased Album",
      artistId = ArtistId("a3"),
      songs = List(Song(SongId("s4"), "Almost There", 200)),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Scheduled
    )
    releaseService.createRelease(release)

    val result = searchService.searchSimilar("Almost There")

    assert(result.isEmpty)
  }
}
