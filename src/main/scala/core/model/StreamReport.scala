package test.ice
package core.model

final case class StreamReport(
                         songId: SongId,
                         title: String,
                         monetizedCount: Int,
                         nonMonetizedCount: Int
                       )
