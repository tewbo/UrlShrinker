package service

import domain.{FullUrl, UrlKey}

trait UrlKeyGenerator {
  def generate(fullUrl: FullUrl, seed: Long): UrlKey
}

object UrlKeyGenerator {
  private class UrlKeyGeneratorImpl() extends UrlKeyGenerator {
    override def generate(fullUrl: FullUrl, seed: Long): UrlKey = {
      UrlKey(seed.toString)
    }
  }

  def make: UrlKeyGenerator = new UrlKeyGeneratorImpl()
}
