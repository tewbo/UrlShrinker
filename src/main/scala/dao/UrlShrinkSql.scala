package dao

import cats.{Applicative, Monad}
import cats.syntax.applicative._
import cats.syntax.either._
import domain._
import domain.errors._
import doobie._
import doobie.implicits._

trait UrlShrinkSql {
  def insertShrunkUrlSql(key: String, fullUrl: String): ConnectionIO[Either[Error, String]]

  def getShrunkUrlSql(url: String): ConnectionIO[String]

  def getFullUrlSql(key: String): ConnectionIO[String]
}

object UrlShrinkSql {
  object sqls {
    def insertShrunkUrlSql(key: String, fullUrl: String): Update0 =
      sql"""insert into URLS (url_key, full_url)
          values ($key, $fullUrl)
        """.update

    def getShrunkUrlSql(url: String): Query0[String] =
      sql"""
            select keu from URLS where full_url = $url
        """.query[String]

    def getFullUrlSql(key: String): Query0[String] =
      sql"""
            select full_url from URLS where url_key = $key
           """.query[String]
  }
}
