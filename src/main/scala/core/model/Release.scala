package test.ice
package core.model

import java.time.LocalDate

final case class ReleaseId(value: String) extends AnyVal

sealed trait ReleaseStatus
object ReleaseStatus {
  case object Draft extends ReleaseStatus
  case object PendingApproval extends ReleaseStatus
  case object Scheduled extends ReleaseStatus
  case object Released extends ReleaseStatus
  case object TakenDown extends ReleaseStatus
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