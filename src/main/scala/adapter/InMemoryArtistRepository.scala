package test.ice
package adapter

import core.model._
import core.port.ArtistRepository
import scala.collection.mutable

class InMemoryArtistRepository extends ArtistRepository {

  private val store: mutable.Map[ArtistId, Artist] = mutable.Map.empty

  def save(artist: Artist): Unit = {
    store.update(artist.id, artist)
  }

  override def findById(id: ArtistId): Option[Artist] = store.get(id)
}
