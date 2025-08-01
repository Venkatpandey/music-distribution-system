package test.ice

import AppModule.*
import core.model.*

import java.time.LocalDate
import java.util.UUID

object Main {
  def main(args: Array[String]): Unit = {
    println("Music Distribution System Demo Starting...\n")

    // Step 1: Create a labeled artist
    val artistId = ArtistId("a-demo")
    val artist = Artist(artistId, "Demo Artist", Some(RecordLabel("rlId1","Universal")))
    artistRepo.save(artist)
    println(s"[Artist Created] → ${artist.name} (${artist.id.value})\n")

    // Step 2: Create a draft release with 2 songs
    val releaseId = ReleaseId("r-demo")
    val song1 = Song(SongId("s1"), "Dream State", 180)
    val song2 = Song(SongId("s2"), "Echo Chamber", 200)

    val release = Release(
      id = releaseId,
      title = "Demo Album",
      artistId = artistId,
      songs = List(song1, song2),
      proposedDate = None,
      agreedDate = None,
      status = ReleaseStatus.Draft
    )
    val creation = releaseService.createRelease(release)
    println(s"[Release Created] → ${release.title} (${release.id.value}) = $creation\n")

    // Step 3: Propose release date
    val proposedDate = LocalDate.now().plusDays(1)
    val proposed = releaseService.proposeReleaseDate(releaseId, proposedDate)
    println(s"[Proposed Date] → $proposedDate = $proposed\n")

    // Step 4: Agree on release date
    val agreed = releaseService.agreeToReleaseDate(releaseId)
    println(s"[Agreed Date] → $agreed\n")

    // Step 5: Manually mark release as Released (simulating date reach)
    val released = release.copy(status = ReleaseStatus.Released, agreedDate = Some(proposedDate))
    releaseRepo.save(released)
    println(s"[Manually Marked as Released] → ${released.title}\n")

    // Step 6: Record some streams
    val stream1 = streamingService.recordStream(song1.id, 45)
    val stream2 = streamingService.recordStream(song2.id, 20)
    println(s"[Stream Recorded] ${song1.title} = $stream1")
    println(s"[Stream Recorded] ${song2.title} = $stream2\n")

    // Step 7: Generate artist's stream report
    val report = reportService.generateReport(artistId)
    println(s"[Stream Report]")
    report.foreach(r =>
      println(s"• ${r.title} | Monetized: ${r.monetizedCount}, Non-Monetized: ${r.nonMonetizedCount}")
    )
    println()

    // Step 8: File for payment
    val payment = paymentService.fileForPayment(artistId)
    println(s"[Payment Filed] → $payment\n")

    // Step 9: Search for similar song
    val matches = searchService.searchSimilar("dream")
    println("[Search Results for 'dream']:")
    matches.foreach(s => println(s"• ${s.title}"))
    println()

    // Step 10: Withdraw release
    val withdrawn = releaseService.withdrawRelease(releaseId)
    println(s"[Withdrawn Release] → $withdrawn\n")

    // Step 11: Try to stream after withdrawal (should fail)
    val blocked = streamingService.recordStream(song1.id, 60)
    println(s"[Post-Withdrawal Stream Attempt] → $blocked\n")

    println("Demo complete.")
  }
}
