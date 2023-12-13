package model

import derevo.circe.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
case class FullUrl(string: String)

object FullUrl {
  def apply(url: String): FullUrl = {
    new FullUrl(
      if (url.startsWith("http://") || url.startsWith("https://"))
        url
      else
        "http://" + url
    )
  }
}

@derive(encoder, decoder)
case class UrlKey(string: String)

sealed trait ComputedUrlKey {
  val key: String
}

@derive(encoder, decoder)
case class ExistingUrlKey(key: String) extends ComputedUrlKey

@derive(encoder, decoder)
case class CreatedUrlKey(key: String) extends ComputedUrlKey

case class UrlRecordId(id: Long)



