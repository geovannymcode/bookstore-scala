package com.bookstore.catalog.config

import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.typesafe._
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.flywaydb.core.Flyway

/** Configuración del catálogo
 */
final case class CatalogConfig(
                                name: String,
                                pageSize: Int
                              )

/** Configuración del pool de conexiones
 */
final case class PoolConfig(
                             minimumIdle: Int,
                             maximumPoolSize: Int,
                             connectionTimeout: Long,
                             idleTimeout: Long,
                             maxLifetime: Long
                           )

/** Configuración de la base de datos
 */
final case class DatabaseConfig(
                                 url: String,
                                 username: String,
                                 password: String,
                                 driver: String,
                                 pool: PoolConfig
                               )

/** Configuración HTTP
 */
final case class HttpConfig(
                             host: String,
                             port: Int
                           )

/** Configuración completa de la aplicación
 */
final case class AppConfig(
                            catalog: CatalogConfig,
                            database: DatabaseConfig,
                            http: HttpConfig
                          )

object AppConfig {

  /** Layer que proporciona la configuración
   */
  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer {
      ZIO.config[AppConfig](deriveConfig[AppConfig].mapKey(toKebabCase))
    }

  /** Crea un DataSource de HikariCP
   */
  def createDataSource(dbConfig: DatabaseConfig): Task[HikariDataSource] =
    ZIO.attempt {
      val hikariConfig = new HikariConfig()
      hikariConfig.setJdbcUrl(dbConfig.url)
      hikariConfig.setUsername(dbConfig.username)
      hikariConfig.setPassword(dbConfig.password)
      hikariConfig.setDriverClassName(dbConfig.driver)
      hikariConfig.setMinimumIdle(dbConfig.pool.minimumIdle)
      hikariConfig.setMaximumPoolSize(dbConfig.pool.maximumPoolSize)
      hikariConfig.setConnectionTimeout(dbConfig.pool.connectionTimeout)
      hikariConfig.setIdleTimeout(dbConfig.pool.idleTimeout)
      hikariConfig.setMaxLifetime(dbConfig.pool.maxLifetime)

      new HikariDataSource(hikariConfig)
    }

  /** Ejecuta migraciones de Flyway
   */
  def runMigrations(dataSource: HikariDataSource): Task[Int] =
    ZIO.attempt {
      val flyway = Flyway
        .configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()

      flyway.migrate().migrationsExecuted
    }

  /** Layer que proporciona DataSource con migraciones ejecutadas
   */
  val dataSourceLayer: ZLayer[AppConfig, Throwable, HikariDataSource] =
    ZLayer.scoped {
      for {
        config     <- ZIO.service[AppConfig]
        _          <- ZIO.logInfo(s"Creating DataSource for ${config.database.url}")
        dataSource <- createDataSource(config.database)
        migrations <- runMigrations(dataSource)
        _          <- ZIO.logInfo(s"Executed $migrations migrations")
        _          <- ZIO.addFinalizer(ZIO.attempt(dataSource.close()).orDie)
      } yield dataSource
    }
}