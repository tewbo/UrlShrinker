package service

import cats.Id
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import dao.UrlShrinkSql
import domain.{ComputedUrlKey, FullUrl, UrlKey}
import domain.errors._
import doobie.syntax.connectionio._
import doobie.util.transactor.Transactor
import tofu.logging.Logging
import tofu.logging.Logging.Make
import tofu.syntax.logging._


trait UrlShrinkStorage {
  def insertUrlRecord(fullUrl: FullUrl): IO[Either[AppError, ComputedUrlKey]]

  def getFullUrlByUrlKey(urlKey: UrlKey): IO[Either[AppError, FullUrl]]

  def getTotalRecordCount: Long
}

object UrlShrinkStorage {

  private final class DatabaseImpl(urlShrinkSql: UrlShrinkSql, transactor: Transactor[IO], urlKeyGenerator: UrlKeyGenerator) extends UrlShrinkStorage {

    // TODO: rewrite with good error handling and keys generation with squid library
    override def getTotalRecordCount: Long = {
      urlShrinkSql.getTotalRecordCount.transact(transactor).unsafeRunSync()
    }

    override def insertUrlRecord(fullUrl: FullUrl): IO[Either[AppError, ComputedUrlKey]] = {
      val key = urlKeyGenerator.generate(fullUrl, getTotalRecordCount)
      urlShrinkSql.insertUrlKey(key, fullUrl)
        .transact(transactor)
        .attempt
        .map {
          case Left(th) => InternalError(th).asLeft
          case Right(computedUrlKey) => computedUrlKey.asRight
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


  private final class LoggingImpl(storage: UrlShrinkStorage)(implicit
                                                             logging: Logging[IO]
  ) extends UrlShrinkStorage {
    private def surroundWithLogs[Error, Res](io: IO[Either[Error, Res]])
                                            (inputLog: String)
                                            (errorOutputLog: Error => (String, Option[Throwable]))
                                            (successOutputLog: Res => String): IO[Either[Error, Res]] =
      info"$inputLog" *> io.flatTap {
        case Left(error) =>
          val (logString: String, throwable: Option[Throwable]) = errorOutputLog(error)
          throwable.fold(error"$logString")(err =>
            errorCause"$logString"(err)
          )
        case Right(success) => info"${successOutputLog(success)}"
      }

    override def insertUrlRecord(fullUrl: FullUrl): IO[Either[AppError, ComputedUrlKey]] =
      surroundWithLogs(storage.insertUrlRecord(fullUrl))(s"insertUrlRecord($fullUrl)") {
        case InternalError(cause) => ("insertUrlRecord failed", cause.some)
        case error => ("insertUrlRecord failed", error.cause)
      } {
        computedUrlKey: ComputedUrlKey => s"insertUrlRecord succeeded with key ${computedUrlKey.key}"
      }


    override def getFullUrlByUrlKey(urlKey: UrlKey): IO[Either[AppError, FullUrl]] = {
      surroundWithLogs(storage.getFullUrlByUrlKey(urlKey))(s"getFullUrlByUrlKey($urlKey)") {
        case InternalError(cause) => ("getFullUrlByUrlKey failed", cause.some)
        case error => ("getFullUrlByUrlKey failed", error.cause)
      } {
        fullUrl: FullUrl => s"getFullUrlByUrlKey succeeded with url ${fullUrl.url}"
      }
    }

    override def getTotalRecordCount: Long = {
      storage.getTotalRecordCount
    }
  }

  def make(sql: UrlShrinkSql, transactor: Transactor[IO], urlKeyGenerator: UrlKeyGenerator): UrlShrinkStorage = {
    //    new DatabaseImpl(sql, transactor, urlKeyGenerator)
    val logs: Make[IO] = Logging.Make.plain[IO]
    implicit val logging: Id[Logging[IO]] = logs.forService[UrlShrinkStorage]
    new LoggingImpl(new DatabaseImpl(sql, transactor, urlKeyGenerator))
  }
}
