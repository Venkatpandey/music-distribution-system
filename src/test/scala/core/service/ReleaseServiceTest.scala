package test.ice
package core.service

import adapter.{InMemoryArtistRepository, InMemoryReleaseRepository}
import core.error.AppError
import core.model.*

import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDate
import java.util.UUID

class ReleaseServiceTest extends AnyFunSuite {
    test("create release with empty ID should generate new ID and save") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val release = Release(
        id = ReleaseId(""),
        title = "Empty ID Release",
        artistId = ArtistId("a1"),
        songs = List.empty,
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Draft
      )

      val result = service.createRelease(release)

      assert(result.isRight)
      val saved = result.toOption.get
      assert(saved.id.value.nonEmpty)
      assert(repo.findById(saved.id).isDefined)
    }

    test("create release with existing ID should fail") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val existing = Release(
        id = ReleaseId("r1"),
        title = "Existing Release",
        artistId = ArtistId("a1"),
        songs = List.empty,
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Draft
      )
      repo.save(existing)

      val duplicate = existing.copy(title = "Duplicate")

      val result = service.createRelease(duplicate)

      assert(result.isLeft)
    }

    test("create new release with new ID should succeed") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val newId = ReleaseId(UUID.randomUUID().toString)
      val release = Release(
        id = newId,
        title = "Valid Release",
        artistId = ArtistId("a2"),
        songs = List.empty,
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Draft
      )

      val result = service.createRelease(release)

      assert(repo.findById(newId).contains(release))
    }


    test("artist adds a song via service with repo injected new") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val releaseId = ReleaseId("r1")
      val release = Release(
        id = releaseId,
        title = "My Demo",
        artistId = ArtistId("a1"),
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Draft
      )

      assert(service.createRelease(release).isRight)

      val song = Song(SongId("s1"), "My Song", 180)

      assert(service.addSongToRelease(releaseId, song).get.message.equals("Success"))

      val updated = repo.findById(releaseId).get
      assert(updated.songs.contains(song))
    }

    test("artist can propose a release date for a draft release") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val releaseId = ReleaseId("r1")
      val release = Release(
        id = releaseId,
        title = "Upcoming Album",
        artistId = ArtistId("a1"),
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Draft
      )

      repo.save(release)

      val proposedDate = LocalDate.now.plusDays(7)
      val result = service.proposeReleaseDate(releaseId, proposedDate)

      assert(result.isRight)

      val updated = repo.findById(releaseId).get
      assert(updated.proposedDate.contains(proposedDate))
      assert(updated.status == ReleaseStatus.PendingApproval)
    }

    test("cannot propose release date if release is not in Draft status") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val releaseId = ReleaseId("r2")
      val release = Release(
        id = releaseId,
        title = "Locked Album",
        artistId = ArtistId("a2"),
        songs = List.empty,
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Scheduled
      )

      repo.save(release)

      val proposedDate = LocalDate.now.plusDays(10)
      val result = service.proposeReleaseDate(releaseId, proposedDate)

      assert(result == Left(AppError.InvalidState))
    }

    test("cannot propose release date if release has no songs") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val releaseId = ReleaseId("r-empty")
      val release = Release(
        id = releaseId,
        title = "Empty Release",
        artistId = ArtistId("a1"),
        songs = List.empty,
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Draft
      )

      repo.save(release)

      val proposedDate = LocalDate.now.plusDays(5)
      val result = service.proposeReleaseDate(releaseId, proposedDate)

      assert(result == Left(AppError("Cannot propose release date with no songs")))
    }


    test("should successfully agree to release date when artist has label") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val artistId = ArtistId("a1")
      val releaseId = ReleaseId("r1")
      val date = LocalDate.now.plusDays(7)

      val artist = Artist(artistId, "Artist With Label", Some(RecordLabel("l1", "Label")))
      artistRepo.save(artist)

      val release = Release(
        id = releaseId,
        title = "Labelled Release",
        artistId = artistId,
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = Some(date),
        agreedDate = None,
        status = ReleaseStatus.PendingApproval
      )
      releaseRepo.save(release)

      val result = service.agreeToReleaseDate(releaseId)

      assert(result.isRight)

      val updated = releaseRepo.findById(releaseId).get
      assert(updated.agreedDate.contains(date))
      assert(updated.status == ReleaseStatus.Scheduled)
    }

    test("should fail if release is not in PendingApproval status") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val artistId = ArtistId("a2")
      val releaseId = ReleaseId("r2")

      val artist = Artist(artistId, "Labelled Artist", Some(RecordLabel("l2", "Label")))
      artistRepo.save(artist)

      val release = Release(
        id = releaseId,
        title = "Wrong State Release",
        artistId = artistId,
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = Some(LocalDate.now.plusDays(3)),
        agreedDate = None,
        status = ReleaseStatus.Draft
      )
      releaseRepo.save(release)

      val result = service.agreeToReleaseDate(releaseId)

      assert(result == Left(AppError.InvalidState))
    }

    test("should fail if artist has no label") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val artistId = ArtistId("a3")
      val releaseId = ReleaseId("r3")

      val artist = Artist(artistId, "Unlabeled Artist", None)
      artistRepo.save(artist)

      val release = Release(
        id = releaseId,
        title = "Unlabeled Artist Release",
        artistId = artistId,
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = Some(LocalDate.now.plusDays(4)),
        agreedDate = None,
        status = ReleaseStatus.PendingApproval
      )
      releaseRepo.save(release)

      val result = service.agreeToReleaseDate(releaseId)

      assert(result == Left(AppError("Artist has no label. Cannot approve release.")))
    }

    test("should fail if artist does not exist") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val releaseId = ReleaseId("r4")

      val release = Release(
        id = releaseId,
        title = "Missing Artist Release",
        artistId = ArtistId("non-existent"),
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = Some(LocalDate.now.plusDays(5)),
        agreedDate = None,
        status = ReleaseStatus.PendingApproval
      )
      releaseRepo.save(release)

      val result = service.agreeToReleaseDate(releaseId)

      assert(result == Left(AppError("Artist not found")))
    }

    test("should distribute release if agreed date is today or earlier and status is Scheduled") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val releaseId = ReleaseId("r1")
      val agreedDate = LocalDate.now.minusDays(1)

      val release = Release(
        id = releaseId,
        title = "Ready to Distribute",
        artistId = ArtistId("a1"),
        songs = List(Song(SongId("s1"), "Streamable", 150)),
        proposedDate = Some(agreedDate),
        agreedDate = Some(agreedDate),
        status = ReleaseStatus.Scheduled
      )
      releaseRepo.save(release)

      val result = service.distributeIfDue(releaseId, LocalDate.now)

      assert(result.isRight)

      val updated = releaseRepo.findById(releaseId).get
      assert(updated.status == ReleaseStatus.Released)
    }

    test("should fail if release status is not Scheduled") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val releaseId = ReleaseId("r2")

      val release = Release(
        id = releaseId,
        title = "Wrong State",
        artistId = ArtistId("a1"),
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = Some(LocalDate.now),
        agreedDate = Some(LocalDate.now),
        status = ReleaseStatus.PendingApproval
      )
      releaseRepo.save(release)

      val result = service.distributeIfDue(releaseId, LocalDate.now)

      assert(result == Left(AppError.InvalidState))
    }

    test("should fail if agreed date is in the future") {
      val releaseRepo = new InMemoryReleaseRepository()
      val artistRepo = new InMemoryArtistRepository()
      val service = new ReleaseService(releaseRepo, artistRepo)

      val releaseId = ReleaseId("r3")

      val release = Release(
        id = releaseId,
        title = "Future Release",
        artistId = ArtistId("a1"),
        songs = List(Song(SongId("s1"), "Track", 120)),
        proposedDate = Some(LocalDate.now.plusDays(3)),
        agreedDate = Some(LocalDate.now.plusDays(3)),
        status = ReleaseStatus.Scheduled
      )
      releaseRepo.save(release)

      val result = service.distributeIfDue(releaseId, LocalDate.now)

      assert(result == Left(AppError("Release date has not been reached yet")))
    }

    test("should take down a released release") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val release = Release(
        id = ReleaseId("r1"),
        title = "Ready to Retire",
        artistId = ArtistId("a1"),
        songs = List(Song(SongId("s1"), "Track 1", 180)),
        proposedDate = None,
        agreedDate = None,
        status = ReleaseStatus.Released
      )
      repo.save(release)

      val result = service.withdrawRelease(release.id)

      assert(result.isRight)
      val updated = repo.findById(release.id).get
      assert(updated.status == ReleaseStatus.TakenDown)
    }

    test("should not take down a draft or scheduled release") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val draft = Release(ReleaseId("d1"), "Draft", ArtistId("a1"), Nil, None, None, ReleaseStatus.Draft)
      val scheduled = draft.copy(id = ReleaseId("s1"), status = ReleaseStatus.Scheduled)

      repo.save(draft)
      repo.save(scheduled)

      val result1 = service.withdrawRelease(draft.id)
      val result2 = service.withdrawRelease(scheduled.id)

      assert(result1 == Left(AppError("Only released releases can be taken down")))
      assert(result2 == Left(AppError("Only released releases can be taken down")))
    }

    test("should return error if release not found") {
      val repo = new InMemoryReleaseRepository()
      val service = new ReleaseService(repo, InMemoryArtistRepository())

      val result = service.withdrawRelease(ReleaseId("nonexistent"))

      assert(result == Left(AppError("Release not found")))
    }

}
