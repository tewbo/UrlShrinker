package controller

import cats.effect.IO
import domain.errors.{AppError, InternalError}
import sttp.tapir.server.ServerEndpoint
import service.UrlShrinkStorage

trait UrlShrinkController {
  def shrinkUrl: ServerEndpoint[Any, IO]
  def expandUrl: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object UrlShrinkController {
  final class Impl(storage: UrlShrinkStorage) extends UrlShrinkController {
    override val shrinkUrl: ServerEndpoint[Any, IO] =
      endpoints.shrinkUrl.serverLogic { case (context, url) =>
        storage.shrinkUrl(url)
      }

    override val expandUrl: ServerEndpoint[Any, IO] =
      endpoints.expandUrl.serverLogic { case (key, context) =>
        storage.expandUrl(key)
      }

    override val all: List[ServerEndpoint[Any, IO]] =
      List(shrinkUrl, expandUrl)
  }

  def make(storage: UrlShrinkStorage): UrlShrinkController = new Impl(storage)
}
