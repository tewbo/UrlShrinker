import cats.effect.kernel.Sync
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import config.ServerConf
import controller.UrlShrinkController
import io.circe.Encoder.AsObject.importedAsObjectEncoder
import io.circe._
import io.circe.generic.auto.exportEncoder
import org.http4s._
import org.http4s.implicits._
import io.circe.generic.semiauto._
import org.http4s.LiteralSyntaxMacros.uri
import org.http4s.Method.GET
import org.http4s.headers.Location

import scala.collection.mutable
//import org.http4s.Status.{Ok, TemporaryRedirect}
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.http4sLiteralsSyntax
import cats.effect._
import cats.syntax.all._
import org.http4s._, org.http4s.dsl.io._, org.http4s.implicits._
import org.http4s.server.Router
import pureconfig.ConfigSource
import service.UrlShrinkStorage
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.{Endpoint, endpoint, stringBody}
import tofu.logging.Logging
import org.http4s.client.middleware.FollowRedirect

object Application extends IOApp {
  private type Init[A] = IO[A]
  private type App[A] = IO[A]

  private val logger = Logging.Make.plain[IO].forService[Application.type]

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info("Starting service....")
      conf = ConfigSource.default
      server <- Sync[IO].delay(conf.at("server").loadOrThrow[ServerConf])
      storage = UrlShrinkStorage.make()
      controller = UrlShrinkController.make(storage)


      openApi = OpenAPIDocsInterpreter()
        .toOpenAPI(es = controller.all.map(_.endpoint), "Example", "1.0")
        .toYaml

      routes = Http4sServerInterpreter[IO]().toRoutes(SwaggerUI[IO](openApi) ++ controller.all)
      httpApp = Router("/" -> routes).orNotFound
      service: EmberServerBuilder[IO]
        = EmberServerBuilder
        .default[IO]
        .withPort(Port.fromInt(server.port).getOrElse(port"8080"))
        .withHttpApp(httpApp)
    } yield service).flatMap(_.build.useForever).as(ExitCode.Success)
  }
}

object Meow {
  sealed trait Cat {
    val meow: String
  }

  case class Kitten(meow: String) extends Cat

  case class WhiteCat(meow: String) extends Cat

  def main(args: Array[String]): Unit = {
    val mapa = mutable.Map[Cat, Int]()
    mapa.put(WhiteCat("meow"), 1)
    println(mapa.getOrElse(WhiteCat("meow"), 2))
  }
}