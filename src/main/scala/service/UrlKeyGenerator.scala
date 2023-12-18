package service

import cats.Monad
import cats.effect.IO
import cats.implicits.toFunctorOps
import domain.{FullUrl, UrlKey}

trait UrlKeyGenerator[F[_]] {
  def generate(fullUrl: FullUrl, seed: F[Long]): F[UrlKey] // todo remove fullUrl as it is not needed
}

object UrlKeyGenerator {
  private class UrlKeyGeneratorImpl[F[_] : Monad]() extends UrlKeyGenerator[F] {
    override def generate(fullUrl: FullUrl, seed: F[Long]): F[UrlKey] = {
      seed.map(_.toString).map(UrlKey(_))
    }

  }

  def make[F[_]: Monad]: UrlKeyGenerator[F] = new UrlKeyGeneratorImpl[F]()
}
