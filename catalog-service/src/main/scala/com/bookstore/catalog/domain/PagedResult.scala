package com.bookstore.catalog.domain

/**
 * Clase para resultados paginados
 */
case class PagedResult[T](
                           data: Seq[T],
                           totalElements: Long,
                           pageNumber: Int,
                           totalPages: Int,
                           isFirst: Boolean,
                           isLast: Boolean,
                           hasNext: Boolean,
                           hasPrevious: Boolean
                         )
