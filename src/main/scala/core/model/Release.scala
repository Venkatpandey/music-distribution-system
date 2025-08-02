package test.ice
package core.model

import java.time.LocalDate

final case class ReleaseId(value: String) extends AnyVal

// ReleaseStatus status of release that it go through in its lifetime
sealed trait ReleaseStatus
object ReleaseStatus {
  case object Draft extends ReleaseStatus // initial status
  case object PendingApproval extends ReleaseStatus // artist proposed a release data
  case object Scheduled extends ReleaseStatus // proposed released agreed
  case object Released extends ReleaseStatus // released when date is reached
  case object TakenDown extends ReleaseStatus // artist take out release
}

final case class Release(
                          id: ReleaseId,
                          title: String,
                          artistId: ArtistId,
                          songs: List[Song] = List.empty,
                          proposedDate: Option[LocalDate] = None,
                          agreedDate: Option[LocalDate] = None,
                          status: ReleaseStatus = ReleaseStatus.Draft
                        )