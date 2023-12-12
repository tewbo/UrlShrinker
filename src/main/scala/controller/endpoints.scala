package controller

import domain.RequestContext
import domain.errors.AppError
import model.{CreatedShrunkUrl, ExistingShrunkUrl, ShrunkUrl, ReceivedShrunkUrl}
import sttp.model.StatusCode
import sttp.model
import sttp.tapir.EndpointIO.annotations.statusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.{PublicEndpoint, endpoint, oneOf, oneOfVariant, path, stringBody, stringToPath}

object endpoints {
  val shrinkUrl: Endpoint[Unit, (RequestContext, String), AppError, ShrunkUrl, Any] = {
    endpoint.post
      .in("shrink")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[String])
      .errorOut(jsonBody[AppError])
      .out(oneOf[ShrunkUrl](
          oneOfVariant(StatusCode.Ok, jsonBody[ExistingShrunkUrl]),
          oneOfVariant(StatusCode.Created, jsonBody[CreatedShrunkUrl])
      ))
  }
    val expandUrl: Endpoint[Unit, (String, RequestContext), AppError, String, Any] = {
      endpoint.post
//        .in("expand")
        .in(path[String])
        .in(header[RequestContext]("X-Request-Id"))
        .errorOut(jsonBody[AppError])
        .out(jsonBody[String])
    }
}
