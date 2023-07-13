//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.ktx

import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.pdf.PdfDocument.PageInfo.Builder
import io.mockk.EqMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import java.io.OutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class PdfDocumentTest {

    private val pdfDocument = mockk<PdfDocument>(relaxed = true)
    private val pdfPageInfo = mockk<PageInfo>()
    private val pdfPage = mockk<Page>()

    @Test
    fun `addPage should open and close for drawing`() {
        val draw = mockk<Page.() -> Unit>(relaxed = true)

        every { pdfDocument.startPage(pdfPageInfo) } returns pdfPage

        runBlocking { pdfDocument.addPage(pdfPageInfo, draw = draw) }

        verifyOrder {
            pdfDocument.startPage(pdfPageInfo)
            pdfPage.draw()
            pdfDocument.finishPage(pdfPage)
        }
    }

    @Test
    fun `addPage should open and close on error`() {
        every { pdfDocument.startPage(pdfPageInfo) } returns pdfPage

        assertFailsWith<IllegalStateException> {
            runBlocking {
                pdfDocument.addPage(pdfPageInfo) {
                    throw IllegalStateException()
                }
            }
        }

        verifyOrder {
            pdfDocument.startPage(pdfPageInfo)
            pdfDocument.finishPage(pdfPage)
        }
    }

    @Test
    fun `use should return the result`() {
        assertEquals(3, pdfDocument.use { 3 })
    }

    @Test
    fun `use should close the document on success`() {
        val out = mockk<OutputStream>()

        assertEquals(Unit, pdfDocument.use { it.writeTo(out) })

        verifyOrder {
            pdfDocument.writeTo(out)
            pdfDocument.close()
        }
    }

    @Test
    fun `use should close the document on error`() {
        assertFailsWith<IllegalStateException> {
            pdfDocument.use { throw IllegalStateException() }
        }

        verifyOrder {
            pdfDocument.close()
        }
    }

    @Test
    fun `PageInfo should create a new instance`() {
        mockkConstructor(Builder::class) {
            val width = 2345
            val height = 4567
            val number = 37

            every {
                constructedWith<Builder>(
                    EqMatcher(width),
                    EqMatcher(height),
                    EqMatcher(number),
                ).setContentRect(null)
            } answers { self as Builder }

            every {
                constructedWith<Builder>(
                    EqMatcher(width),
                    EqMatcher(height),
                    EqMatcher(number),
                ).create()
            } returns pdfPageInfo

            assertEquals(pdfPageInfo, PageInfo(width, height, number))
        }
    }

    @Test
    fun `PageInfo should create a new instance with content rect`() {
        mockkConstructor(Builder::class) {
            val width = 2345
            val height = 4567
            val number = 37
            val rect = mockk<Rect>()

            every {
                constructedWith<Builder>(
                    EqMatcher(width),
                    EqMatcher(height),
                    EqMatcher(number),
                ).setContentRect(rect)
            } answers { self as Builder }

            every {
                constructedWith<Builder>(
                    EqMatcher(width),
                    EqMatcher(height),
                    EqMatcher(number),
                ).create()
            } returns pdfPageInfo

            assertEquals(pdfPageInfo, PageInfo(width, height, number, rect))
        }
    }
}
