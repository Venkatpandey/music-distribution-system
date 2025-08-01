package test.ice
package core.model

final case class RecordLabel(
                              id: String,
                              name: String
                            )
final case class ArtistId(value: String) extends AnyVal
final case class Artist(
                         id: ArtistId,
                         name: String,
                         label: Option[RecordLabel]
                       )
