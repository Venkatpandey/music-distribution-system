package test.ice
package core.model

import java.time.Instant

final case class Payment(
                          artistId: ArtistId,
                          paidAt: Instant,
                          totalStreams: Int,
                          totalAmount: BigDecimal
                  )
