package test.ice
package core.model

final case class SongId(value: String) extends AnyVal

// Song and its duration in seconds
final case class Song(
                       id: SongId,
                       title: String,
                       durationInSeconds: Int
                     )
