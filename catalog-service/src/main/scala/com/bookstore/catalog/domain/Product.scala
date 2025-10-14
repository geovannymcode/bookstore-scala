package com.bookstore.catalog.domain

/**
 * Modelo de dominio para un producto (libro)
 */
case class Product(
                    id: Option[Long] = None,
                    code: String,
                    name: String,
                    description: Option[String] = None,
                    imageUrl: Option[String] = None,
                    price: BigDecimal
                  )
