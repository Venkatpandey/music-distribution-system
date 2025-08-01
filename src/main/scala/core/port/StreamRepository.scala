package test.ice
package core.port

import core.model._

trait StreamRepository {
  def save(stream: Stream): Unit
  def all(): List[Stream]
}

