import cats.effect.kernel.Sync
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import config.{DbConf, ServerConf}
import controller.UrlShrinkController
import dao.UrlShrinkSql
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import pureconfig.ConfigSource
import service.{UrlKeyGenerator, UrlShrinkStorage}
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import tofu.logging.Logging

object Main extends IOApp {
  private type Init[A] = IO[A]

  private val logger = Logging.Make.plain[IO].forService[Main.type]

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- logger.info("Starting service....")
      conf = ConfigSource.default
      db <- Sync[Init].delay(conf.at("db").loadOrThrow[DbConf])
      transactor = Transactor.fromDriverManager[IO](
        db.driver,
        db.url,
        db.user,
        db.password
      )
      sql = UrlShrinkSql.make
      urlKeyGenerator = UrlKeyGenerator.make[ConnectionIO]
      storage = UrlShrinkStorage.make(sql, transactor, urlKeyGenerator)
      controller = UrlShrinkController.make(storage)

      server <- Sync[IO].delay(conf.at("server").loadOrThrow[ServerConf])

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