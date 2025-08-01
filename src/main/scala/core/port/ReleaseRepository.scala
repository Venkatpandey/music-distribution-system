package test.ice
package core.port

import core.model._

trait ReleaseRepository {
  def save(release: Release): Unit

  def findById(id: ReleaseId): Option[Release]

  def all(): List[Release]
}

