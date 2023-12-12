package model

import derevo.circe.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
sealed trait ShrunkUrl {
  val url: String
}
@derive(encoder, decoder)
case class ExistingShrunkUrl(url: String) extends ShrunkUrl
@derive(encoder, decoder)
case class CreatedShrunkUrl(url: String) extends ShrunkUrl
@derive(encoder, decoder)
case class ReceivedShrunkUrl(url: String) extends ShrunkUrl


