package controller

import domain.RequestContext
import domain.errors.AppError
import model.{CreatedShrunkUrl, ExistingShrunkUrl, ReceivedShrunkUrl, ShrunkUrl}
import org.http4s.headers.`Content-Location`
import model._
import sttp.model.StatusCode
import sttp.tapir.EndpointIO.annotations.statusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.{PublicEndpoint, endpoint, oneOf, oneOfVariant, path, stringBody, stringToPath}

object endpoints {
  val shrinkUrl: Endpoint[Unit, (RequestContext, String), AppError, ComputedUrlKey, Any] = {
    endpoint.post
      .in("shrink")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[String])
      .errorOut(jsonBody[AppError])
      .out(oneOf[ComputedUrlKey](
          oneOfVariant(StatusCode.Ok, jsonBody[ExistingUrlKey]),
          oneOfVariant(StatusCode.Created, jsonBody[CreatedUrlKey])
      ))
  }
    val expandUrl: Endpoint[Unit, String, AppError, String, Any] = {
      endpoint.get
        .in(path[String])
//        .in(header[RequestContext]("X-Request-Id"))   // TODO: ask about request-id
        .errorOut(jsonBody[AppError])
//        .out(jsonBody[String])
        .out(header[String]("Location"))
        .out(statusCode(StatusCode.Found))
    }
}
