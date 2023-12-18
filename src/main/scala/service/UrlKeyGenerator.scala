package service

import cats.Monad
import cats.implicits.toFunctorOps
import domain.UrlKey
import sqids.Sqids

trait UrlKeyGenerator[F[_]] {
  def generate(seed: F[Long]): F[UrlKey]
}

object UrlKeyGenerator {
  private class SqidsUrlKeyGeneratorImpl[F[_] : Monad]() extends UrlKeyGenerator[F] {
    override def generate(seed: F[Long]): F[UrlKey] = {
      val sqids = Sqids.default
      seed.map(sqids.encodeUnsafeString(_)).map(UrlKey(_))
    }
  }

  def make[F[_]: Monad]: UrlKeyGenerator[F] = new SqidsUrlKeyGeneratorImpl[F]()
}
