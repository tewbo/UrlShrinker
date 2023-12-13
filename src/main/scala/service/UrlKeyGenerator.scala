package service

import model.{FullUrl, UrlKey, CreatedUrlKey}

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
