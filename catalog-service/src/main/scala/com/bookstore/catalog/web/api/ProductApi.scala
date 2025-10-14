package com.bookstore.catalog.web.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.bookstore.catalog.domain.{ProductNotFoundException, ProductService}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
 * API REST para productos usando Akka HTTP
 */
class ProductApi(productService: ProductService) extends LazyLogging {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "catalog-api")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val route: Route =
    pathPrefix("api" / "products") {
      concat(
        // GET /api/products?page=1
        (get & pathEndOrSingleSlash) {
          parameter("page".as[Int].withDefault(1)) { pageNo =>
            logger.info(s"Fetching products for page: $pageNo")
            complete(productService.getProducts(pageNo))
          }
        },
        // GET /api/products/{code}
        (get & path(Segment)) { code =>
          logger.info(s"Fetching product for code: $code")
          productService.getProductByCode(code) match {
            case Some(product) => complete(product)
            case None =>
              complete(
                StatusCodes.NotFound,
                ProductNotFoundException.forCode(code).getMessage
              )
          }
        }
      )
    }

  /**
   * Inicia el servidor HTTP
   */
  def start(host: String, port: Int, terminationDeadline: FiniteDuration = 10.seconds): Unit = {
    val bindingFuture = Http().newServerAt(host, port).bind(route)

    bindingFuture.onComplete {
      case scala.util.Success(binding) =>
        logger.info(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
      case scala.util.Failure(ex) =>
        logger.error(s"Failed to bind HTTP server: ${ex.getMessage}", ex)
        system.terminate()
    }

    // Graceful shutdown
    sys.addShutdownHook {
      logger.info("Shutting down HTTP server...")
      bindingFuture
        .flatMap(_.terminate(terminationDeadline))
        .onComplete { _ =>
          logger.info("HTTP server terminated, shutting down ActorSystem...")
          system.terminate()
        }
    }
  }
}