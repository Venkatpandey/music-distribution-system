package test.ice
package adapter


import core.model._
import core.port.ReleaseRepository

import scala.collection.mutable

class InMemoryReleaseRepository extends ReleaseRepository {

  private val store: mutable.Map[ReleaseId, Release] = mutable.Map.empty

  override def save(release: Release): Unit =
    store.update(release.id, release)

  override def findById(id: ReleaseId): Option[Release] =
    store.get(id)

  override def findByArtist(artistId: ArtistId): List[Release] =
    store.values.filter(_.artistId == artistId).toList

  override def all(): List[Release] =
    store.values.toList
}

