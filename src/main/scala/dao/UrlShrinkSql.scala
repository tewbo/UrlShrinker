package dao

import cats.syntax.applicative._
import domain.{ComputedUrlKey, CreatedUrlKey, ExistingUrlKey, FullUrl, UrlKey, UrlRecordId}
import doobie._
import doobie.implicits._
import service.UrlKeyGenerator

trait UrlShrinkSql {
  def insertUrlKey(fullUrl: FullUrl, urlKeyGenerator: UrlKeyGenerator[ConnectionIO]): doobie.ConnectionIO[ComputedUrlKey]

  def getUrlKeyByFullUrl(fullUrl: FullUrl): ConnectionIO[Option[UrlKey]]

  def getFullUrlByUrlKey(urlKey: UrlKey): ConnectionIO[Option[FullUrl]]

  def getTotalRecordCount: ConnectionIO[Long]
}

object UrlShrinkSql {
  object sqls {
    def insertUrlKeySql(key: UrlKey, fullUrl: FullUrl): Update0 =
      sql"""insert into URLS (url_key, full_url)
          values (${key.key}, ${fullUrl.url})
        """.update

    def getUrlKeyByFullUrlSql(url: FullUrl): Query0[UrlKey] =
      sql"""
            select url_key from URLS where full_url = ${url.url}
        """.query[UrlKey]

    def getFullUrlByUrlKeySql(key: UrlKey): Query0[FullUrl] =
      sql"""
            select full_url from URLS where url_key = ${key.key}
           """.query[FullUrl]

    def getTotalRecordCountSql: Query0[Long] =
      sql"""
           select count(*) from URLS
           """.query[Long]
  }

  private final class Impl extends UrlShrinkSql {

    import sqls._

    override def insertUrlKey(fullUrl: FullUrl, urlKeyGenerator: UrlKeyGenerator[ConnectionIO]): ConnectionIO[ComputedUrlKey] =
      getUrlKeyByFullUrlSql(fullUrl).option.flatMap {
        case Some(foundUrlKey) =>
          ExistingUrlKey(foundUrlKey.key).pure[ConnectionIO].map[ComputedUrlKey](identity)
        case None =>
          urlKeyGenerator.generate(getTotalRecordCount).flatMap(urlKey => insertUrlKeySql(urlKey, fullUrl)
            .withUniqueGeneratedKeys[UrlRecordId]("id")
            .map(id => CreatedUrlKey(urlKey.key)).map[ComputedUrlKey](identity))
      }

    override def getUrlKeyByFullUrl(fullUrl: FullUrl): ConnectionIO[Option[UrlKey]] =
      getUrlKeyByFullUrlSql(fullUrl).option

    override def getFullUrlByUrlKey(urlKey: UrlKey): ConnectionIO[Option[FullUrl]] =
      getFullUrlByUrlKeySql(urlKey).option

    override def getTotalRecordCount: ConnectionIO[Long] = getTotalRecordCountSql.unique
  }

  def make: UrlShrinkSql = new Impl()
}
