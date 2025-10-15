package com.bookstore.catalog.web

import zio._
import zio.http._
import zio.json._
import com.bookstore.catalog.domain._

/** API REST para productos usando ZIO HTTP
 */
object ProductApi {

  /** Crea las rutas HTTP para productos
   */
  def routes: Http[ProductService, Nothing, Request, Response] =
    Http.collectZIO[Request] {

      // GET /api/products?page=1
      case req @ Method.GET -> Root / "api" / "products" =>
        val pageNo = req.url.queryParams.get("page").flatMap(_.headOption).flatMap(_.toIntOption).getOrElse(1)

        getProducts(pageNo)
          .foldZIO(
            error => ZIO.succeed(errorResponse(error)),
            result => ZIO.succeed(Response.json(result.toJson))
          )

      // GET /api/products/{code}
      case Method.GET -> Root / "api" / "products" / code =>
        getProductByCode(code)
          .foldZIO(
            error => ZIO.succeed(errorResponse(error)),
            product => ZIO.succeed(Response.json(product.toJson))
          )
    }

  /** Obtiene productos paginados
   */
  private def getProducts(pageNo: Int): ZIO[ProductService, CatalogError, PagedResult[Product]] =
    for {
      _      <- ZIO.logInfo(s"Fetching products for page: $pageNo")
      service <- ZIO.service[ProductService]
      result <- service.getProducts(pageNo)
    } yield result

  /** Obtiene un producto por código
   */
  private def getProductByCode(code: String): ZIO[ProductService, CatalogError, Product] =
    for {
      _       <- ZIO.logInfo(s"Fetching product for code: $code")
      service <- ZIO.service[ProductService]
      product <- service.getProductByCode(code)
    } yield product

  /** Convierte un error en una respuesta HTTP
   */
  private def errorResponse(error: CatalogError): Response = error match {
    case CatalogError.ProductNotFound(code) =>
      Response
        .text(s"Product with code '$code' not found")
        .withStatus(Status.NotFound)

    case CatalogError.ValidationError(field, reason) =>
      Response
        .text(s"Validation error in field '$field': $reason")
        .withStatus(Status.BadRequest)

    case CatalogError.DatabaseError(cause) =>
      Response
        .text(s"Database error: ${cause.getMessage}")
        .withStatus(Status.InternalServerError)

    case CatalogError.ConfigurationError(reason) =>
      Response
        .text(s"Configuration error: $reason")
        .withStatus(Status.InternalServerError)

    case CatalogError.ApplicationError(reason, _) =>
      Response
        .text(s"Application error: $reason")
        .withStatus(Status.InternalServerError)
  }
}