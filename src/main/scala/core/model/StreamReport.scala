package test.ice
package core.model

// StreamReport keeps track of monetized / unmonetized counts
final case class StreamReport(
                         songId: SongId,
                         title: String,
                         monetizedCount: Int,
                         nonMonetizedCount: Int
                       )
