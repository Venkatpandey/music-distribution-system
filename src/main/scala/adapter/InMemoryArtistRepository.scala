package test.ice
package adapter

import core.model._
import core.port.ArtistRepository
import scala.collection.concurrent.TrieMap

class InMemoryArtistRepository extends ArtistRepository {
  
  private val store = TrieMap.empty[ArtistId, Artist]

  def save(artist: Artist): Unit = {
    store.update(artist.id, artist)
  }

  override def findById(id: ArtistId): Option[Artist] = store.get(id)
}
