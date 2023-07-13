//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.Dispatchers.IO
import kotlin.coroutines.CoroutineContext

val LocalCoroutineContext = compositionLocalOf<CoroutineContext> { IO }

@[Composable ReadOnlyComposable]
operator fun ProvidableCompositionLocal<CoroutineContext>.plus(
    context: CoroutineContext,
) = provides(current + context)
