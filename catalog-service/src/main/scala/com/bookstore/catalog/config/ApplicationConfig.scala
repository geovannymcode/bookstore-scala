package com.bookstore.catalog.config

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.flywaydb.core.Flyway
import com.typesafe.scalalogging.LazyLogging
import pureconfig._
import pureconfig.generic.auto._

class ApplicationConfig extends LazyLogging {

  // Cargar configuración desde application.conf
  private val config: AppConfig = ConfigSource.default.loadOrThrow[AppConfig]

  logger.info(s"Loading configuration for service: ${config.catalog.name}")
  logger.info(s"Database URL: ${config.database.url}")
  logger.info(s"HTTP Server: ${config.http.host}:${config.http.port}")

  // Exponer configuraciones
  val pageSize: Int    = config.catalog.pageSize
  val httpHost: String = config.http.host
  val httpPort: Int    = config.http.port

  def dataSource(): HikariDataSource = {
    val hikariConfig = new HikariConfig()

    hikariConfig.setJdbcUrl(config.database.url)
    hikariConfig.setUsername(config.database.username)
    hikariConfig.setPassword(config.database.password)
    hikariConfig.setDriverClassName(config.database.driver)

    // Configuración del pool
    hikariConfig.setMinimumIdle(config.database.pool.minimumIdle)
    hikariConfig.setMaximumPoolSize(config.database.pool.maximumPoolSize)
    hikariConfig.setConnectionTimeout(config.database.pool.connectionTimeout)
    hikariConfig.setIdleTimeout(config.database.pool.idleTimeout)
    hikariConfig.setMaxLifetime(config.database.pool.maxLifetime)

    logger.info(
      "Creating HikariCP DataSource with pool size: " +
        s"${config.database.pool.minimumIdle}-${config.database.pool.maximumPoolSize}"
    )

    new HikariDataSource(hikariConfig)
  }

  def runMigrations(ds: HikariDataSource): Unit = {
    logger.info("Running database migrations...")
    val flyway = Flyway
      .configure()
      .dataSource(ds)
      .locations("classpath:db/migration")
      .load()

    val result = flyway.migrate()
    logger.info(s"Migrations completed. Applied ${result.migrationsExecuted} migrations")
  }
}
