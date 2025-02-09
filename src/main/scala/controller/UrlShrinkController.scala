package controller

import cats.effect.IO
import domain.FullUrl
import service.UrlShrinkStorage
import sttp.tapir.server.ServerEndpoint

trait UrlShrinkController {
  def shrinkUrl: ServerEndpoint[Any, IO]
  def expandUrl: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object UrlShrinkController {
  final class Impl(storage: UrlShrinkStorage) extends UrlShrinkController {
    override val shrinkUrl: ServerEndpoint[Any, IO] =
      Endpoints.shrinkUrl.serverLogic { case (context, url) =>
        val correctUrl = FullUrl(url)
        storage.insertUrlRecord(correctUrl)
      }

    override def expandUrl: ServerEndpoint[Any, IO] = {
      Endpoints.expandUrl.serverLogic { case key =>
        storage.getFullUrlByUrlKey(key)
      }
    }

    override val all: List[ServerEndpoint[Any, IO]] =
      List(shrinkUrl, expandUrl)
  }

  def make(storage: UrlShrinkStorage): UrlShrinkController = new Impl(storage)
}
