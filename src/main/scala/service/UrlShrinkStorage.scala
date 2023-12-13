package service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxEitherId
import dao.UrlShrinkSql
import domain.errors._
import doobie.syntax.connectionio._
import doobie.util.transactor.Transactor
import model._

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

  def make(sql: UrlShrinkSql, transactor: Transactor[IO], urlKeyGenerator: UrlKeyGenerator): UrlShrinkStorage = {
    new DatabaseImpl(sql, transactor, urlKeyGenerator)
  }
}
