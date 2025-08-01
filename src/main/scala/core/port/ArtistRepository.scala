package test.ice
package core.port

import core.model._

trait ArtistRepository {
  def findById(id: ArtistId): Option[Artist]
  def save(artist: Artist): Unit
}
