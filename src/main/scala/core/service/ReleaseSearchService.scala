package test.ice
package core.service

import core.model._
import core.port.{ReleaseRepository, SearchService}

class ReleaseSearchService(releaseRepo: ReleaseRepository) extends SearchService {
  private val minSimilarity = 0.7 // Uses a fixed similarity threshold of 0.8 (i.e. 80% match or better).

  // searchSimilar uses levenshtein to find matching songs for the
  // given song title.
  def searchSimilar(title: String): List[Song] = {
    releaseRepo
      .all()
      .filter(_.status == ReleaseStatus.Released)
      .flatMap(_.songs)
      .filter { song =>
        val similarity = computeSimilarity(song.title.toLowerCase, title.toLowerCase)
        similarity >= minSimilarity
      }
  }

  // Computes a normalized similarity score between two strings using Levenshtein distance.
  // The result ranges from 0.0 (completely different) to 1.0 (exact match),
  // making it suitable for fuzzy string comparison regardless of string length.
  private def computeSimilarity(a: String, b: String): Double = {
    val distance = levenshtein(a, b)
    val maxLen = math.max(a.length, b.length)
    if (maxLen == 0) 1.0 else 1.0 - (distance.toDouble / maxLen)
  }


  // https://en.wikipedia.org/wiki/Levenshtein_distance
  // https://rosettacode.org/wiki/Levenshtein_distance#Scala
  private def levenshtein(a: String, b: String): Int = {
    val lenA = a.length
    val lenB = b.length

    val distance = Array.ofDim[Int](lenA + 1, lenB + 1)

    for (i <- 0 to lenA) distance(i)(0) = i
    for (j <- 0 to lenB) distance(0)(j) = j

    for {
      i <- 1 to lenA
      j <- 1 to lenB
    } {
      val cost = if (a.charAt(i - 1) == b.charAt(j - 1)) 0 else 1

      val deletion = distance(i - 1)(j) + 1
      val insertion = distance(i)(j - 1) + 1
      val substitution = distance(i - 1)(j - 1) + cost

      distance(i)(j) = Seq(deletion, insertion, substitution).min
    }

    distance(lenA)(lenB)
  }

}
