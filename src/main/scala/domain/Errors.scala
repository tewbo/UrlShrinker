package domain

import cats.syntax.option._
import derevo.circe.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

object Errors {
  @derive(encoder, decoder)
  sealed abstract class AppError(
    val message: String,
    val cause: Option[Throwable] = None
  )

  @derive(encoder, decoder)
  case class FullUrlNotFound() extends AppError("Full url not found")

  @derive(encoder, decoder)
  case class InternalError(
    cause0: Throwable
  ) extends AppError("Internal error", cause0.some)
  @derive(encoder, decoder)
  case class DecodedError(override val message: String) extends AppError(message = message)

  implicit val throwableEncoder: Encoder[Throwable] =
    Encoder.encodeString.contramap(_.getMessage)
  implicit val throwableDecoder: Decoder[Throwable] =
    Decoder.decodeString.map(new Throwable(_))
  implicit val schema: Schema[AppError] =
    Schema.schemaForString.map[AppError](str => Some(DecodedError(str)))(
      _.message
    )
}
