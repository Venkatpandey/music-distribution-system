package test.ice
package adapter

import core.model._
import core.port.StreamRepository

import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._

class InMemoryStreamRepository extends StreamRepository {
  
  private val store = new ConcurrentLinkedQueue[Stream]()

  override def save(stream: Stream): Unit = store.add(stream)

  override def all(): List[Stream] = store.iterator().asScala.toList
}
