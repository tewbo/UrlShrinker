package service

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import config.DbConf
import dao.UrlShrinkSql
import domain.{CreatedUrlKey, FullUrl, UrlKey, ExistingUrlKey}
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import domain.Errors.FullUrlNotFound

class UrlShrinkStorageSpec extends AnyFlatSpec with Matchers {
  def loadStorage(): UrlShrinkStorage = {
    val conf = ConfigSource.default
    val db = Sync[IO].delay(conf.at("db").loadOrThrow[DbConf]).unsafeRunSync()
    val transactor = Transactor.fromDriverManager[IO](
      db.driver,
      db.url,
      db.user,
      db.password
    )
    val sql = UrlShrinkSql.make
    val urlKeyGenerator = UrlKeyGenerator.make[ConnectionIO]
    UrlShrinkStorage.make(sql, transactor, urlKeyGenerator)
  }

  val storage: UrlShrinkStorage = loadStorage()

  "insertUrlRecord" should "insert new url record and return created url key" in {
    val fullUrl = FullUrl("http://insertion_test1.com")
    val result = storage.insertUrlRecord(fullUrl).unsafeRunSync()
    result match {
      case Left(_) => fail("UrlShrinkStorage.insertUrlRecord should return Right")
      case Right(ExistingUrlKey(key)) =>
        fail(
          s"UrlShrinkStorage.insertUrlRecord with new record" +
            s" should return CreatedUrlKey, but returned ExistingUrlKey($key)"
        )
      case Right(CreatedUrlKey(key)) => succeed
    }
  }

  it should "return ExistingUrlKey if url record already exists" in {
    val fullUrl = FullUrl("http://insertion_test2.com")
    val result = for {
      _ <- storage.insertUrlRecord(fullUrl)
      secondInsertionResult <- storage.insertUrlRecord(fullUrl)
    } yield secondInsertionResult
    result.unsafeRunSync() match {
      case Left(_) => fail("UrlShrinkStorage.insertUrlRecord should return Right")
      case Right(CreatedUrlKey(key)) =>
        fail(
          s"UrlShrinkStorage.insertUrlRecord with existing record" +
            s" should return ExistingUrlKey, but returned CreatedUrlKey($key)"
        )
      case Right(ExistingUrlKey(key)) => succeed
    }
  }

  "getFullUrlByKey" should "find the same FullUrl as inserted before" in {
    val fullUrl = FullUrl("http://example.com")
    val result = for {
      resp <- storage.insertUrlRecord(fullUrl)
      urlKeyString = resp.getOrElse(fail("UrlShrinkStorage.insertUrlRecord should return Right")).key
      computedFullUrl <- storage.getFullUrlByUrlKey(UrlKey(urlKeyString))
    } yield computedFullUrl
    result.unsafeRunSync() shouldBe Right(fullUrl)
  }

  it should "throws error if url key is not found" in {
    val result = storage.getFullUrlByUrlKey(UrlKey("not_found_key"))
    result.unsafeRunSync() shouldBe Left(FullUrlNotFound())
  }
}
