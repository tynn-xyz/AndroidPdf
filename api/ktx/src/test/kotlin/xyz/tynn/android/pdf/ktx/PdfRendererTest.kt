//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.ktx

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page
import android.os.ParcelFileDescriptor
import io.mockk.EqMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verifyAll
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PdfRendererTest {

    private val pdfRenderer = mockk<PdfRenderer>(relaxed = true)
    private val pdfPage = mockk<Page>(relaxed = true)

    @Test
    fun `PdfRenderer should delegate to primary constructor`() {
        mockkConstructor(PdfRenderer::class) {
            val fd = mockk<ParcelFileDescriptor>(relaxed = true)
            val assetFd = mockk<AssetFileDescriptor> {
                every { parcelFileDescriptor } returns fd
            }

            every {
                constructedWith<PdfRenderer>(EqMatcher(fd)).pageCount
            } returns 4

            assertEquals(4, PdfRenderer(assetFd).pageCount)
        }
    }

    @Test
    fun `pages should open and close every page`() {
        val pages = listOf(pdfPage, mockk(relaxed = true))

        every { pdfRenderer.pageCount } returns pages.size
        every { pdfRenderer.openPage(any()) } answers {
            pages[firstArg()]
        }

        assertEquals(pages, pdfRenderer.pages.toList())

        verifyOrder {
            pages.forEachIndexed { index, page ->
                pdfRenderer.openPage(index)
                page.close()
            }
        }
    }

    @Test
    fun `withPage should open and close page for rendering`() {
        val render = mockk<Page.() -> Unit>(relaxed = true)

        every { pdfRenderer.openPage(3) } returns pdfPage

        runBlocking { pdfRenderer.withPage(3, render = render) }

        verifyOrder {
            pdfRenderer.openPage(3)
            pdfPage.render()
            pdfPage.close()
        }
    }

    @Test
    fun `render should create an image of the page`() {
        mockkStatic(Bitmap::class) {
            val width = 1234
            val height = 5678
            val bitmap = mockk<Bitmap>()
            val destClip = mockk<Rect>()
            val transform = mockk<Matrix>()
            val renderMode = 234

            every { createBitmap(width, height, ARGB_8888) } returns bitmap

            pdfPage.render(width, height, renderMode, destClip, transform)

            verifyAll {
                pdfPage.render(bitmap, destClip, transform, renderMode)
            }
        }
    }
}
