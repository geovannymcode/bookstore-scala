package com.bookstore.catalog.domain

/**
 * Excepción para producto no encontrado
 */
class ProductNotFoundException(message: String) extends RuntimeException(message)

object ProductNotFoundException {
  def forCode(code: String): ProductNotFoundException =
    new ProductNotFoundException(s"Product with code $code not found")
}
