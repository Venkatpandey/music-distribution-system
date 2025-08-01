package test.ice
package adapter

import core.model._
import core.port.StreamRepository

import scala.collection.mutable

class InMemoryStreamRepository extends StreamRepository {

  private val store: mutable.ListBuffer[Stream] = mutable.ListBuffer.empty

  override def save(stream: Stream): Unit = store += stream

  override def all(): List[Stream] = store.toList
}
