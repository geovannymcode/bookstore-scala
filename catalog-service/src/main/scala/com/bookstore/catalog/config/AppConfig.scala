package com.bookstore.catalog.config

import scala.concurrent.duration.FiniteDuration

// Modelos para la configuración
case class CatalogConfig(name: String, pageSize: Int)

case class DatabaseConfig(
  url: String,
  username: String,
  password: String,
  driver: String,
  pool: PoolConfig
)

case class PoolConfig(
  minimumIdle: Int,
  maximumPoolSize: Int,
  connectionTimeout: Long,
  idleTimeout: Long,
  maxLifetime: Long
)

case class HttpConfig(
  host: String,
  port: Int,
  terminationDeadline: FiniteDuration
)

case class SwaggerConfig(apiGatewayUrl: String)

case class AppConfig(
  catalog: CatalogConfig,
  database: DatabaseConfig,
  http: HttpConfig,
  swagger: SwaggerConfig
)
