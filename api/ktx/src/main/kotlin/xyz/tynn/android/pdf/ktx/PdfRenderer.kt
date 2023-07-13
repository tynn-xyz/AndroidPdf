//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

@file:[JvmMultifileClass JvmName("AndroidPdf")]

package xyz.tynn.android.pdf.ktx

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page
import kotlinx.coroutines.runInterruptible
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a new instance from an [AssetFileDescriptor]
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun PdfRenderer(
    input: AssetFileDescriptor,
): PdfRenderer = PdfRenderer(
    input.parcelFileDescriptor,
)

/**
 * Creates a sequence of all pages in [PdfRenderer]
 *
 * **Note:** All pages are closed automatically and calling
 * [PdfRenderer.Page.close] manually will throw an
 * [IllegalStateException]
 *
 * @see PdfRenderer.openPage
 */
public val PdfRenderer.pages: Sequence<Page>
    get() = sequence {
        for (index in 0 until pageCount)
            openPage(index).use { yield(it) }
    }

/**
 * Opens the [Page] at [index] and closes it after [render]
 */
public suspend inline fun <R> PdfRenderer.withPage(
    index: Int,
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline render: Page.() -> R,
): R = runInterruptible(context) {
    openPage(index).use(render)
}

/**
 * Renders the [Page] to an [ARGB_8888] bitmap
 * with [width] and [height]
 *
 * @see createBitmap
 * @see Page.render
 */
public fun Page.render(
    width: Int,
    height: Int,
    renderMode: Int,
    destClip: Rect? = null,
    transform: Matrix? = null,
): Bitmap = createBitmap(
    width,
    height,
    ARGB_8888,
).also { bitmap ->
    render(
        bitmap,
        destClip,
        transform,
        renderMode,
    )
}
