package test.ice
package adapter


import core.model._
import core.port.ReleaseRepository

import scala.collection.concurrent.TrieMap

class InMemoryReleaseRepository extends ReleaseRepository {

  private val store = TrieMap.empty[ReleaseId, Release]

  override def save(release: Release): Unit =
    store.update(release.id, release)

  override def findById(id: ReleaseId): Option[Release] =
    store.get(id)

  override def all(): List[Release] =
    store.values.toList
}

