package test.ice
package core.model

// RecordLabel to which an artist affiliates to
final case class RecordLabel(
                              id: String,
                              name: String
                            )
final case class ArtistId(value: String) extends AnyVal

// Artist who produces song
final case class Artist(
                         id: ArtistId,
                         name: String,
                         label: Option[RecordLabel]
                       )
