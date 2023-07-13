//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.example

import android.content.res.Configuration
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class PtConverterTest {

    private val pxInPt = 123
    private val ptConverter = PtConverter(pxInPt)

    @Test
    fun `px should convert px into pt`() {
        with(ptConverter) {
            assertEquals(1, pxInPt.px)
        }
    }

    @Test
    fun `pt should convert pt into px`() {
        with(ptConverter) {
            assertEquals(2 * pxInPt, 2.pt)
        }
    }

    @Test
    fun `PtConverter should calculate the correct pt value`() {
        val configuration = mockk<Configuration> {
            densityDpi = pxInPt * 72 + pxInPt / 3
        }

        with(PtConverter(configuration)) {
            assertEquals(pxInPt, 1.pt)
        }
    }

    @Test
    fun `key should be Key`() {
        assertSame(PtConverter.Key, ptConverter.key)
    }

    @Test
    fun `withPtConverter should run with converter from context`() {
        runBlocking(ptConverter) {
            withPtConverter {
                assertEquals(ptConverter, this)
            }
        }
    }

    @Test
    fun `withPtConverter should provide identity converter`() {
        runBlocking {
            withPtConverter {
                assertEquals(PtConverter(1), this)
            }
        }
    }
}
