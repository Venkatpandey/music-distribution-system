package test.ice
package core.model

import java.time.Instant

// Streams are immutable events recorded and queried in aggregates
final case class Stream(
                         songId: SongId,
                         releaseId: ReleaseId,
                         artistId: ArtistId,
                         duration: Int, // in seconds
                         streamedAt: Instant,
                         monetized: Boolean
                 )
