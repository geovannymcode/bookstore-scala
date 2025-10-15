package com.bookstore.catalog.domain

import zio._
import io.getquill._
import io.getquill.jdbczio.Quill
import javax.sql.DataSource

/** Trait para el repositorio de productos
 */
trait ProductRepository {
  def findAll(page: Int, pageSize: Int): IO[CatalogError, (List[Product], Long)]
  def findByCode(code: String): IO[CatalogError, Option[Product]]
}

/** Implementación del repositorio usando Quill + ZIO
 */
final case class ProductRepositoryLive(quill: Quill.Postgres[SnakeCase.type])
  extends ProductRepository {

  import quill._

  // Schema mapping
  inline given productSchema: SchemaMeta[Product] =
    schemaMeta[Product]("products")

  override def findAll(page: Int, pageSize: Int): IO[CatalogError, (List[Product], Long)] = {
    val offset = page * pageSize

    val productsQuery = quote {
      query[Product]
        .drop(lift(offset))
        .take(lift(pageSize))
        .sortBy(p => p.name)(Ord.asc)
    }

    val countQuery = quote {
      query[Product].size
    }

    for {
      products <- run(productsQuery)
        .mapError(err => CatalogError.DatabaseError(err))
      total    <- run(countQuery)
        .mapError(err => CatalogError.DatabaseError(err))
    } yield (products, total)
  }

  override def findByCode(code: String): IO[CatalogError, Option[Product]] = {
    val byCodeQuery = quote {
      query[Product].filter(_.code == lift(code))
    }

    run(byCodeQuery)
      .map(_.headOption)
      .mapError(err => CatalogError.DatabaseError(err))
  }
}

/** Layer para dependency injection
 */
object ProductRepository {

  val live: ZLayer[Quill.Postgres[SnakeCase.type], Nothing, ProductRepository] =
    ZLayer {
      for {
        quill <- ZIO.service[Quill.Postgres[SnakeCase.type]]
      } yield ProductRepositoryLive(quill)
    }
}