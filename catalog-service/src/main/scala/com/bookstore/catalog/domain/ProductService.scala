package com.bookstore.catalog.domain

import com.typesafe.scalalogging.LazyLogging

/**
 * Servicio para gestión de productos
 */
class ProductService(productRepository: ProductRepository, pageSize: Int) extends LazyLogging {

  /**
   * Obtiene productos paginados
   */
  def getProducts(pageNo: Int): PagedResult[Product] = {
    logger.info(s"Getting products for page: $pageNo")

    val effectivePageNo = if (pageNo <= 1) 0 else pageNo - 1
    val (products, totalElements) = productRepository.findAll(effectivePageNo, pageSize)

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

  /**
   * Obtiene un producto por su código
   */
  def getProductByCode(code: String): Option[Product] = {
    logger.info(s"Getting product by code: $code")
    productRepository.findByCode(code)
  }
}
