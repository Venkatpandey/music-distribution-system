package test.ice
package core.service

import core.error.AppError
import core.model.*
import core.port.{ReleaseRepository, StreamRepository}

import java.time.Instant

class StreamingService(releaseRepo: ReleaseRepository, streamRepo: StreamRepository) {

  // Each call to recordStream represents a separate playback
  def recordStream(songId: SongId, duration: Int): Either[AppError, Unit] = {
    if (duration <= 0) return Left(AppError("Duration must be positive"))

    val releaseOpt = releaseRepo
      .all()
      .find(r => r.songs.exists(_.id == songId))

    releaseOpt match {
      case None => Left(AppError("Song not found in any release"))

      case Some(release) if release.status == ReleaseStatus.TakenDown =>
        Left(AppError("This release has been taken down"))

      case Some(release) if release.status != ReleaseStatus.Released =>
        Left(AppError("Release is not active for streaming"))

      case Some(release) =>
        val monetized = duration >= 30 // checks for monetization possible
        val stream = Stream(
          songId = songId,
          releaseId = release.id,
          artistId = release.artistId,
          duration = duration,
          streamedAt = Instant.now(),
          monetized = monetized
        )
        streamRepo.save(stream)
        Right(())
    }
  }

  def bySong(songId: SongId): List[Stream] =
    streamRepo.all().filter(_.songId == songId)

  def byArtist(artistId: ArtistId): List[Stream] =
    streamRepo.all().filter(_.artistId == artistId)
}

