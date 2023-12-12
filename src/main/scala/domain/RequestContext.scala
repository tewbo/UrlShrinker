package domain

import sttp.tapir.Codec
import sttp.tapir.CodecFormat.TextPlain

//@derive(loggable)
final case class RequestContext(requestId: String)
object RequestContext {
  implicit val codec: Codec[String, RequestContext, TextPlain] =
    Codec.string.map(RequestContext(_))(_.requestId)
}
