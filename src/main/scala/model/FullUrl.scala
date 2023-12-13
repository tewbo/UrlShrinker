package model

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Write
import doobie.util.Read
import sttp.tapir.{Codec, CodecFormat, Schema}


@derive(encoder, decoder)
case class FullUrl(url: String)

object FullUrl {
  def apply(url: String): FullUrl = {
    new FullUrl(
      if (url.startsWith("http://") || url.startsWith("https://"))
        url
      else
        "http://" + url
    )
  }

  implicit val schema: Schema[FullUrl] =
    Schema.schemaForString.map(string => Some(FullUrl(string)))(_.url)
  implicit val codec: Codec[String, FullUrl, CodecFormat.TextPlain] =
    Codec.string.map(FullUrl(_))(_.url)
}

@derive(encoder, decoder)
case class UrlKey(key: String)

object UrlKey {
  implicit val schema: Schema[UrlKey] =
    Schema.schemaForString.map(string => Some(UrlKey(string)))(_.key)
  implicit val codec: Codec[String, UrlKey, CodecFormat.TextPlain] =
    Codec.string.map(UrlKey(_))(_.key)
}

sealed trait ComputedUrlKey {
  val key: String
}

@derive(encoder, decoder)
case class ExistingUrlKey(key: String) extends ComputedUrlKey // todo: add ability to accept case class as argument

@derive(encoder, decoder)
case class CreatedUrlKey(key: String) extends ComputedUrlKey

case class UrlRecordId(id: Long)



