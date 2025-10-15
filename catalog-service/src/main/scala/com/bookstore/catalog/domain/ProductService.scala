package com.bookstore.catalog.domain

import zio._

/** Trait para el servicio de productos
 */
trait ProductService {
  def getProducts(pageNo: Int): IO[CatalogError, PagedResult[Product]]
  def getProductByCode(code: String): IO[CatalogError, Product]
}

/** Implementación del servicio
 */
final case class ProductServiceLive(
                                     repository: ProductRepository,
                                     pageSize: Int
                                   ) extends ProductService {

  override def getProducts(pageNo: Int): IO[CatalogError, PagedResult[Product]] = {
    val effectivePageNo = if (pageNo <= 1) 0 else pageNo - 1

    for {
      _      <- ZIO.logInfo(s"Getting products for page: $pageNo")
      result <- repository.findAll(effectivePageNo, pageSize)
    } yield {
      val (products, totalElements) = result
      val totalPages = Math.ceil(totalElements.toDouble / pageSize).toInt

      PagedResult(
        data = products,
        totalElements = totalElements,
        pageNumber = effectivePageNo + 1,
        totalPages = totalPages,
        isFirst = effectivePageNo == 0,
        isLast = effectivePageNo >= totalPages - 1,
        hasNext = effectivePageNo < totalPages - 1,
        hasPrevious = effectivePageNo > 0
      )
    }
  }

  override def getProductByCode(code: String): IO[CatalogError, Product] = {
    for {
      _           <- ZIO.logInfo(s"Getting product by code: $code")
      maybeProduct <- repository.findByCode(code)
      product     <- ZIO.fromOption(maybeProduct)
        .mapError(_ => CatalogError.ProductNotFound(code))
    } yield product
  }
}

/** Layer para dependency injection
 */
object ProductService {

  val live: ZLayer[ProductRepository & Int, Nothing, ProductService] =
    ZLayer {
      for {
        repository <- ZIO.service[ProductRepository]
        pageSize   <- ZIO.service[Int]
      } yield ProductServiceLive(repository, pageSize)
    }

  /** Layer con configuración de pageSize
   */
  def liveWithPageSize(pageSize: Int): ZLayer[ProductRepository, Nothing, ProductService] =
    ZLayer.succeed(pageSize) >>> live
}