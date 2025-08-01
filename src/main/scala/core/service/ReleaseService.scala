package test.ice
package core.service

import core.model.*
import core.port.{ArtistRepository, ReleaseRepository}
import core.error.AppError

import java.time.LocalDate
import java.util.UUID

class ReleaseService(releaseRepo: ReleaseRepository, artistRepo: ArtistRepository) {

  // addSongToRelease adds a song to a given release id only if release is in draft state.
  def addSongToRelease(releaseId: ReleaseId, song: Song): Option[AppError] = {
    releaseRepo.findById(releaseId) match {
      case Some(release) if release.status == ReleaseStatus.Draft =>
        val updated = release.copy(songs = release.songs :+ song)
        releaseRepo.save(updated)
        Some(AppError.Success)
      case Some(_) =>
        Some(AppError.InvalidState)

      case None =>
        Some(AppError.NotFound)
    }
  }

  // createRelease create a release if it doesnt exists
  def createRelease(release: Release): Either[AppError, Release] = {
    val finalRelease =
      if (release.id.value.trim.isEmpty)
        release.copy(id = ReleaseId(UUID.randomUUID().toString))
      else
        release

    releaseRepo.findById(finalRelease.id) match {
      case Some(_) =>
        Left(AppError.InvalidState)

      case None =>
        releaseRepo.save(finalRelease)
        Right(finalRelease)
    }
  }

// proposeReleaseDate helps artist to propose a new release date for a release.
  def proposeReleaseDate(releaseId: ReleaseId, date: LocalDate): Either[AppError, Unit] = {
    releaseRepo.findById(releaseId) match {
      case Some(release) if release.status != ReleaseStatus.Draft =>
        Left(AppError.InvalidState)

      case Some(release) if release.songs.isEmpty =>
        Left(AppError("Cannot propose release date with no songs"))

      case Some(release) if release.status == ReleaseStatus.Draft =>
        val updated = release.copy(
          proposedDate = Some(date),
          status = ReleaseStatus.PendingApproval
        )
        releaseRepo.save(updated)
        Right(())

      case Some(_) =>
        Left(AppError.InvalidState)

      case None =>
        Left(AppError.NotFound)
    }
  }

  def agreeToReleaseDate(releaseId: ReleaseId): Either[AppError, Unit] = {
    releaseRepo.findById(releaseId) match {
      case None =>
        Left(AppError.NotFound)

      case Some(release) if release.status != ReleaseStatus.PendingApproval =>
        Left(AppError.InvalidState)

      case Some(release) =>
        artistRepo.findById(release.artistId) match {
          case None =>
            Left(AppError("Artist not found"))

          case Some(artist) if artist.label.isEmpty =>
            Left(AppError("Artist has no label. Cannot approve release."))

          case Some(_) =>
            val updated = release.copy(
              agreedDate = release.proposedDate,
              status = ReleaseStatus.Scheduled
            )
            releaseRepo.save(updated)
            Right(())
        }
    }
  }

  def distributeIfDue(releaseId: ReleaseId, today: LocalDate): Either[AppError, Unit] = {
    releaseRepo.findById(releaseId) match {
      case None =>
        Left(AppError.NotFound)

      case Some(release) if release.status != ReleaseStatus.Scheduled =>
        Left(AppError.InvalidState)

      case Some(release) if release.agreedDate.exists(_.isAfter(today)) =>
        Left(AppError("Release date has not been reached yet"))

      case Some(release) =>
        val updated = release.copy(status = ReleaseStatus.Released)
        releaseRepo.save(updated)
        Right(())
    }
  }

  def withdrawRelease(releaseId: ReleaseId): Either[AppError, Unit] = {
    releaseRepo.findById(releaseId) match {
      case None =>
        Left(AppError("Release not found"))

      case Some(release) if release.status != ReleaseStatus.Released =>
        Left(AppError("Only released releases can be taken down"))

      case Some(release) =>
        val updated = release.copy(status = ReleaseStatus.TakenDown)
        releaseRepo.save(updated)
        Right(())
    }
  }
}


