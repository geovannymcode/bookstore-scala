package com.bookstore.catalog.domain

import zio.json._

/** Modelo de dominio para un producto (libro)
 */
final case class Product(
                          id: Option[Long],
                          code: String,
                          name: String,
                          description: Option[String],
                          imageUrl: Option[String],
                          price: BigDecimal
                        )

object Product {
  // ZIO JSON codecs para serialización/deserialización
  given JsonDecoder[Product] = DeriveJsonDecoder.gen[Product]
  given JsonEncoder[Product] = DeriveJsonEncoder.gen[Product]

  /** Smart constructor para validar productos
   */
  def make(
            code: String,
            name: String,
            description: Option[String],
            imageUrl: Option[String],
            price: BigDecimal,
            id: Option[Long] = None
          ): Either[CatalogError, Product] = {
    for {
      _ <- validateCode(code)
      _ <- validateName(name)
      _ <- validatePrice(price)
    } yield Product(id, code, name, description, imageUrl, price)
  }

  private def validateCode(code: String): Either[CatalogError, Unit] =
    if (code.trim.nonEmpty) Right(())
    else Left(CatalogError.ValidationError("code", "Code cannot be empty"))

  private def validateName(name: String): Either[CatalogError, Unit] =
    if (name.trim.nonEmpty) Right(())
    else Left(CatalogError.ValidationError("name", "Name cannot be empty"))

  private def validatePrice(price: BigDecimal): Either[CatalogError, Unit] =
    if (price > 0) Right(())
    else Left(CatalogError.ValidationError("price", "Price must be positive"))
}

/** Resultado paginado
 */
final case class PagedResult[T](
                                 data: List[T],
                                 totalElements: Long,
                                 pageNumber: Int,
                                 totalPages: Int,
                                 isFirst: Boolean,
                                 isLast: Boolean,
                                 hasNext: Boolean,
                                 hasPrevious: Boolean
                               )

object PagedResult {
  // ZIO JSON codecs
  given [T: JsonEncoder]: JsonEncoder[PagedResult[T]] =
  DeriveJsonEncoder.gen[PagedResult[T]]

  given [T: JsonDecoder]: JsonDecoder[PagedResult[T]] =
    DeriveJsonDecoder.gen[PagedResult[T]]
}