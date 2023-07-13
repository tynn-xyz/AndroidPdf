//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

@file:[JvmMultifileClass JvmName("AndroidPdfKt")]

package xyz.tynn.android.pdf.ktx

import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.graphics.pdf.PdfDocument.PageInfo
import kotlinx.coroutines.runInterruptible
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Adds a new [Page] to the [PdfDocument]
 *
 * This function calls [PdfDocument.startPage]
 * with [pageInfo] to [draw] the [Page] and finally
 * adds it to the document with [PdfDocument.finishPage].
 */
public suspend inline fun PdfDocument.addPage(
    pageInfo: PageInfo,
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline draw: Page.() -> Unit,
): PdfDocument = apply {
    runInterruptible(context) {
        var page: Page? = null
        try {
            page = startPage(pageInfo)
            page.draw()
        } finally {
            page?.let(::finishPage)
        }
    }
}

/**
 * Treats the [PdfDocument] as an [AutoCloseable] and
 * delegates the [block] to [kotlin.use]
 */
public inline fun <R> PdfDocument.use(
    block: (PdfDocument) -> R,
): R = AutoCloseable { close() }.use {
    block(this)
}

/**
 * Creates a new [PageInfo] with [pageWidth], [pageHeight],
 * [pageNumber] and optional [contentRect]
 */
public fun PageInfo(
    pageWidth: Int,
    pageHeight: Int,
    pageNumber: Int,
    contentRect: Rect? = null,
): PageInfo = PageInfo.Builder(
    pageWidth,
    pageHeight,
    pageNumber,
).setContentRect(
    contentRect,
).create()
