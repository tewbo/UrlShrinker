package controller

import domain.RequestContext
import domain.errors.AppError
import model._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir._

object endpoints {
  val shrinkUrl: Endpoint[Unit, (RequestContext, String), AppError, ComputedUrlKey, Any] = {
    endpoint.post
      .in("shrink")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[String])     // TODO: ask how to parse full url
      .errorOut(jsonBody[AppError])
      .out(oneOf[ComputedUrlKey](
          oneOfVariant(StatusCode.Ok, jsonBody[ExistingUrlKey]),
          oneOfVariant(StatusCode.Created, jsonBody[CreatedUrlKey])
      ))
  }
    val expandUrl: Endpoint[Unit, UrlKey, AppError, FullUrl, Any] = {
      endpoint.get
        .in(path[UrlKey])
//        .in(header[RequestContext]("X-Request-Id"))   // TODO: ask about request-id
        .errorOut(jsonBody[AppError])
        .out(header[FullUrl]("Location"))  // TODO: add russian letters support
        .out(statusCode(StatusCode.Found))
    }
}
