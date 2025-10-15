package com.bookstore.catalog

import zio._
import zio.http._
import zio.logging.backend.SLF4J
import com.bookstore.catalog.config._
import com.bookstore.catalog.domain._
import com.bookstore.catalog.web.ProductApi
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import javax.sql.DataSource

/** Punto de entrada principal de la aplicación
 *
 * Arquitectura funcional con ZIO:
 * - Dependency Injection con ZLayers
 * - Composición funcional pura
 * - Resource management automático
 */
object CatalogServiceApp extends ZIOAppDefault:

  /** Layer de Quill con DataSource
   *
   * Crea el contexto de Quill conectado a DataSource
   */
  private val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  /** Programa principal de la aplicación
   *
   * Compone todos los layers necesarios y ejecuta el servidor
   */
  override def run: ZIO[Any, Any, Any] =
    serverProgram
      .provide(
        // Configuración base
        AppConfig.live,

        // Logging estructurado
        Runtime.removeDefaultLoggers >>> SLF4J.slf4j,

        // Capa de base de datos
        AppConfig.dataSourceLayer,
        quillLayer,

        // Capa de dominio
        ProductRepository.live,
        serviceLayer,

        // Servidor HTTP
        Server.defaultWithPort(8081)
      )
      .tapError(err => ZIO.logErrorCause("Failed to start service", Cause.fail(err)))
      .exitCode

  /** Layer del servicio con configuración de pageSize
   */
  private val serviceLayer: ZLayer[AppConfig & ProductRepository, Nothing, ProductService] =
    ZLayer {
      for
        config <- ZIO.service[AppConfig]
        repo   <- ZIO.service[ProductRepository]
      yield ProductServiceLive(repo, config.catalog.pageSize)
    }

  /** Programa del servidor HTTP
   *
   * Inicia el servidor y mantiene la aplicación corriendo
   */
  private val serverProgram: ZIO[AppConfig & ProductService & Server, Throwable, Unit] =
    for
      config <- ZIO.service[AppConfig]
      _      <- ZIO.logInfo("=" * 60)
      _      <- ZIO.logInfo("🚀 Starting Catalog Service")
      _      <- ZIO.logInfo("=" * 60)
      _      <- ZIO.logInfo(s"📦 Service Name: ${config.catalog.name}")
      _      <- ZIO.logInfo(s"🗄️  Database URL: ${config.database.url}")
      _      <- ZIO.logInfo(s"🌐 HTTP Endpoint: http://localhost:8081")
      _      <- ZIO.logInfo(s"📄 Page Size: ${config.catalog.pageSize}")
      _      <- ZIO.logInfo("=" * 60)

      // Servir HTTP con las rutas de ProductApi (sin Middleware.debug)
      _ <- Server.serve(ProductApi.routes)

    yield ()

end CatalogServiceApp