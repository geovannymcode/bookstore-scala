package com.bookstore.catalog

import com.bookstore.catalog.web.api.ProductApi
import com.bookstore.catalog.config.ApplicationConfig
import com.bookstore.catalog.domain.{ProductRepository, ProductService}
import com.typesafe.scalalogging.LazyLogging

/**
 * Punto de entrada principal de la aplicación
 */
object CatalogServiceApp extends App with LazyLogging {

  logger.info("Starting Catalog Service...")

  try {
    // Inicializar configuración
    val appConfig = new ApplicationConfig()
    val dataSource = appConfig.dataSource()

    // Ejecutar migraciones de base de datos
    appConfig.runMigrations(dataSource)

    // Inicializar repositorios
    val productRepository = new ProductRepository(dataSource)

    // Inicializar servicios
    val productService = new ProductService(productRepository, appConfig.pageSize)

    // Iniciar la API REST
    val productApi = new ProductApi(productService)
    productApi.start(appConfig.httpHost, appConfig.httpPort)

    logger.info("Catalog Service started successfully")

  } catch {
    case ex: Exception =>
      logger.error("Failed to start Catalog Service", ex)
      System.exit(1)
  }
}