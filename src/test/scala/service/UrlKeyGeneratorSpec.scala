package service

import cats.Id
import domain.FullUrl
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UrlKeyGeneratorSpec extends AnyFlatSpec with Matchers {
  "UrlKeyGenerator" should "generate different keys for different seeds" in {
    val generator = UrlKeyGenerator.make[Id]
    val seed1 = 1L
    val seed2 = 2L
    val key1 = generator.generate(seed1)
    val key2 = generator.generate(seed2)
    key1 should not be key2
  }

  it should "generate the same key for the same seed" in {
    val generator = UrlKeyGenerator.make[Id]
    val seed = 1L
    val key1 = generator.generate(seed)
    val key2 = generator.generate(seed)
    key1 shouldBe key2
  }

  it should "work with large numbers" in {
    val generator = UrlKeyGenerator.make[Id]
    val seed = 1234567890123456789L
    val key = generator.generate(seed)
    key.key.nonEmpty shouldBe true
  }
}
