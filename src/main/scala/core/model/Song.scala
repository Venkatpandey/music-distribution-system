package test.ice
package core.model

final case class SongId(value: String) extends AnyVal

final case class Song(
                       id: SongId,
                       title: String,
                       durationInSeconds: Int
                     )
