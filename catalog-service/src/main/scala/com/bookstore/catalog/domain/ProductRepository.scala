package com.bookstore.catalog.domain

import io.getquill._
import com.zaxxer.hikari.HikariDataSource

trait ProductRepositoryContext {
  val ctx: PostgresJdbcContext[SnakeCase.type]

  implicit val productSchema: ctx.SchemaMeta[Product] =
    ctx.schemaMeta[Product]("products")
}

final class ProductRepository(dataSource: HikariDataSource) extends ProductRepositoryContext {

  override val ctx = new PostgresJdbcContext(SnakeCase, dataSource)
  import ctx._

  def findAll(page: Int, pageSize: Int): (List[Product], Long) = {
    val offset = page * pageSize

    val pagedQ = quote {
      query[Product]
        .drop(lift(offset))
        .take(lift(pageSize))
        .sortBy(p => p.name)(Ord.asc)
    }

    val countQ = quote {
      query[Product].size
    }

    val products = run(pagedQ)
    val total    = run(countQ)

    (products, total)
  }

  def findByCode(code: String): Option[Product] = {
    val byCodeQ = quote {
      query[Product].filter(_.code == lift(code))
    }
    run(byCodeQ).headOption
  }

  def close(): Unit = dataSource.close()
}
