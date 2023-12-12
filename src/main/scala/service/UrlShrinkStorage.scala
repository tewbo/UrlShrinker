package service

import scala.collection.mutable
import cats.effect.IO
import domain.errors._
import sttp.tapir.EndpointIO.annotations.statusCode
import sttp.tapir.EndpointOutput.StatusCode
import model._

trait UrlShrinkStorage {
  def shrinkUrl(url: String): IO[Either[InternalError, ShrunkUrl]]
  def expandUrl(url: ShrunkUrl): IO[Either[InternalError, String]]
}

object UrlShrinkStorage {
  private final class InMemory extends UrlShrinkStorage {
    private val LongToShort: mutable.Map[String, String] = mutable.Map[String, String]()
    private val ShortToLong: mutable.Map[String, String] = mutable.Map[String, String]()

    override def shrinkUrl(url: String): IO[Either[InternalError, ShrunkUrl]] = {
      val key = CreatedShrunkUrl(LongToShort.size.toString)
      if (LongToShort.contains(url)) {
        IO.pure(ExistingShrunkUrl(LongToShort.getOrElse(url, ""))).attempt
          .map(_.left.map(InternalError.apply)) // todo: rewrite
      } else {
        LongToShort.put(url, key.url)
        ShortToLong.put(key.url, url)
        IO.pure(key).attempt.map(_.left.map(InternalError.apply))
      }
    }
    override def expandUrl(url: String): IO[Either[InternalError, String]] = {
      IO.pure(ShortToLong.getOrElse(url, ""))
        .attempt
        .map(_.left.map(InternalError.apply))
    }
  }

  def make(): UrlShrinkStorage = new InMemory()
}
