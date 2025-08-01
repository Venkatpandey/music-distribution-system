package test.ice
package core.service

import core.model._
import core.port.{ReleaseRepository, StreamRepository}

class StreamingReportService(
                              streamRepo: StreamRepository,
                              releaseRepo: ReleaseRepository
                            ) {
  
  // Generates a streaming report for the given artist.
  // For each song associated with the artist, it aggregates all recorded streams
  // and calculates how many were monetized (duration >= 30s) and how many were not.
  // Returns a list of StreamReportEntry with monetized and nonmonetized counts.
  def generateReport(artistId: ArtistId): List[StreamReport] = {
    val streams = streamRepo.all().filter(_.artistId == artistId)

    val releases = releaseRepo.all().filter(_.artistId == artistId)

    val songIdToTitle: Map[SongId, String] = releases
      .flatMap(_.songs.map(song => song.id -> song.title))
      .toMap

    streams
      .groupBy(_.songId)
      .map { case (songId, groupedStreams) =>
        val monetized = groupedStreams.count(_.monetized)
        val nonMonetized = groupedStreams.size - monetized
        StreamReport(
          songId = songId,
          title = songIdToTitle(songId),
          monetizedCount = monetized,
          nonMonetizedCount = nonMonetized
        )
      }.toList
  }
}

