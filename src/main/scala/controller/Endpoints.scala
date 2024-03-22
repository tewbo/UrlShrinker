package controller

import domain.Errors.AppError
import domain._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object Endpoints {
  val shrinkUrl: Endpoint[Unit, (RequestContext, String), AppError, ComputedUrlKey, Any] = {
    endpoint.post
      .in("shrink")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[String])
      .errorOut(jsonBody[AppError])
      .out(
        oneOf[ComputedUrlKey](
          oneOfVariant(StatusCode.Ok, jsonBody[ExistingUrlKey]),
          oneOfVariant(StatusCode.Created, jsonBody[CreatedUrlKey])
        )
      )
  }
  val expandUrl: Endpoint[Unit, UrlKey, AppError, FullUrl, Any] = {
    endpoint.get
      .in(path[UrlKey])
      .errorOut(jsonBody[AppError])
      .out(header[FullUrl]("Location"))
      .out(statusCode(StatusCode.Found))
  }
}
