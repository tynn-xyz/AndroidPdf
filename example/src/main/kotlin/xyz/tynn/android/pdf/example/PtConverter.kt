//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.example

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import xyz.tynn.android.pdf.example.PtConverter.Key
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@JvmInline
value class PtConverter(
    private val pxInPt: Int,
) : CoroutineContext.Element {
    override val key get() = Key

    val Int.pt get() = times(pxInPt)
    val Int.px get() = div(pxInPt)

    constructor(configuration: Configuration) : this(
        pxInPt = configuration.densityDpi / 72,
    )

    companion object Key : CoroutineContext.Key<PtConverter>
}

@[Composable ReadOnlyComposable SuppressLint("ComposableNaming")]
fun PtConverter() = PtConverter(LocalConfiguration.current)

suspend inline fun <R> withPtConverter(
    block: PtConverter.() -> R,
) = with(coroutineContext[Key] ?: PtConverter(1), block)
