package test.ice
package core.port

import core.model.Song

trait SearchService {
  def searchSimilar(title: String): List[Song]
}
