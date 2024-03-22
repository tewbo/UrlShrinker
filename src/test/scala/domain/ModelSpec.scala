package domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ModelSpec extends AnyFlatSpec with Matchers {
  "FullUrl" should "add prefix 'http://' if there is no 'http://' or 'https://'" in {
    val url = "example.com"
    val fullUrl = FullUrl(url)
    fullUrl.url shouldBe "http://example.com"
  }

  it should "not add prefix 'http://' if there is 'http://'" in {
    val url = "http://example.com"
    val fullUrl = FullUrl(url)
    fullUrl.url shouldBe "http://example.com"
  }

  it should "not add prefix 'http://' if there is 'https://'" in {
    val url = "https://example.com"
    val fullUrl = FullUrl(url)
    fullUrl.url shouldBe "https://example.com"
  }
}
