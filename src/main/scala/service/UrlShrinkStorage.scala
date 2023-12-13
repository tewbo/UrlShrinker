package service

import scala.collection.mutable
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import dao.UrlShrinkSql
import domain.errors._
import doobie.util.transactor.Transactor
import sttp.tapir.EndpointIO.annotations.statusCode
import sttp.tapir.EndpointOutput.StatusCode
import model._
import doobie._
import doobie.syntax.connectionio._

trait UrlShrinkStorage {
  def insertUrlRecord(fullUrl: FullUrl): IO[Either[AppError, UrlKey]]

  def getFullUrlByUrlKey(urlKey: UrlKey): IO[Either[AppError, FullUrl]]

  def getTotalRecordCount: Long
}

object UrlShrinkStorage {
  /*private final class InMemory extends UrlShrinkStorage {
    private val LongToShort: mutable.Map[String, String] = mutable.Map[String, String]()
    private val ShortToLong: mutable.Map[String, String] = mutable.Map[String, String]()

    override def shrinkUrl(url: String): IO[Either[InternalError, ShrunkUrl]] = {
      val key = CreatedShrunkUrl(LongToShort.size.toString)
      if (LongToShort.contains(url)) {
        IO.pure(ExistingShrunkUrl(LongToShort.getOrElse(url, ""))).attempt
          .map(_.left.map(InternalError.apply)) // todo: rewrite
      } else {
        val correctUrl =
          if (url.startsWith("http://") || url.startsWith("https://"))
            url
          else
            "http://" + url
        LongToShort.put(correctUrl, key.url)
        ShortToLong.put(key.url, correctUrl)
        IO.pure(key).attempt.map(_.left.map(InternalError.apply))
      }
    }
    // todo: russian letters support
    override def expandUrl(url: String): IO[Either[InternalError, String]] = {
      IO.pure(ShortToLong.getOrElse(url, ""))
        .attempt
        .map(_.left.map(InternalError.apply))
    }
  }*/

  private final class DatabaseImpl(urlShrinkSql: UrlShrinkSql, transactor: Transactor[IO], urlKeyGenerator: UrlKeyGenerator) extends UrlShrinkStorage {

    // todo: rewrite with good error handling and keys generation
    override def getTotalRecordCount: Long = {
      urlShrinkSql.getTotalRecordCount.transact(transactor).unsafeRunSync()
    }

    override def insertUrlRecord(fullUrl: FullUrl): IO[Either[AppError, UrlKey]] = {
      val key = urlKeyGenerator.generate(fullUrl, getTotalRecordCount)
      urlShrinkSql.insertUrlKey(key, fullUrl)
        .transact(transactor)
        .attempt
        .map {
          case Left(th) => InternalError(th).asLeft
          case Right(Left(error)) => error.asLeft
          case Right(Right(key)) => key.asRight
        }
    }

    override def getFullUrlByUrlKey(urlKey: UrlKey): IO[Either[AppError, FullUrl]] = {
      urlShrinkSql.getFullUrlByUrlKey(urlKey).transact(transactor)
        .attempt.map {
        case Left(th) => InternalError(th).asLeft
        case Right(None) => FullUrlNotFound().asLeft
        case Right(Some(fullUrl)) => fullUrl.asRight[AppError]
      }
    }
  }

  //  def make(): UrlShrinkStorage = new InMemory()
  def make(sql: UrlShrinkSql, transactor: Transactor[IO], urlKeyGenerator: UrlKeyGenerator): UrlShrinkStorage = {
    new DatabaseImpl(sql, transactor, urlKeyGenerator)
  }
}
