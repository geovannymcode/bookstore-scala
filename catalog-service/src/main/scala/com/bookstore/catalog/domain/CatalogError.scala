package com.bookstore.catalog.domain

/** ADT para errores del dominio de catálogo
 */
sealed trait CatalogError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object CatalogError {

  /** Error cuando un producto no se encuentra
   */
  final case class ProductNotFound(code: String) extends CatalogError {
    override val message: String = s"Product with code '$code' not found"
  }

  /** Error de base de datos
   */
  final case class DatabaseError(cause: Throwable) extends CatalogError {
    override val message: String = s"Database error: ${cause.getMessage}"
  }

  /** Error de configuración
   */
  final case class ConfigurationError(reason: String) extends CatalogError {
    override val message: String = s"Configuration error: $reason"
  }

  /** Error de validación
   */
  final case class ValidationError(field: String, reason: String) extends CatalogError {
    override val message: String = s"Validation error in field '$field': $reason"
  }

  /** Error genérico de aplicación
   */
  final case class ApplicationError(reason: String, cause: Option[Throwable] = None)
    extends CatalogError {
    override val message: String =
      s"Application error: $reason${cause.map(c => s" - ${c.getMessage}").getOrElse("")}"
  }
}
