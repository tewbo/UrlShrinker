package controller

import cats.effect.IO
import domain.errors.{AppError, InternalError}
import sttp.tapir.server.ServerEndpoint
import service.UrlShrinkStorage
import model._

trait UrlShrinkController {
  def shrinkUrl: ServerEndpoint[Any, IO]
//  def expandUrl: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object UrlShrinkController {
  final class Impl() extends UrlShrinkController {
    override val shrinkUrl: ServerEndpoint[Any, IO] =
      endpoints.shrinkUrl.serverLogic { case (context, url) =>
//        storage.shrinkUrl(url)
        val correctUrl = FullUrl(url)
        val key = CreatedUrlKey("123")
        IO.pure(key).attempt.map(_.left.map(InternalError.apply))
      }

    /*override val expandUrl: ServerEndpoint[Any, IO] =
      endpoints.expandUrl.serverLogic { case key =>
        storage.expandUrl(key)
      }*/

    override val all: List[ServerEndpoint[Any, IO]] =
//      List(shrinkUrl, expandUrl)
    List(shrinkUrl)
  }

  def make(): UrlShrinkController = new Impl()
}
