//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.example

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_PRINT
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.withContext
import xyz.tynn.android.pdf.ktx.PdfRenderer
import xyz.tynn.android.pdf.ktx.render
import xyz.tynn.android.pdf.ktx.withPage

@Composable
fun RenderPdf(
    modifier: Modifier = Modifier,
    assetFileName: String = "form.pdf",
    onBitmapChanged: (Bitmap) -> Unit,
) = with(remember { mutableStateOf<Bitmap?>(null) }) {
    RenderPdfImage(value, modifier)
    RenderPdfEffect(assetFileName) {
        value = it
        onBitmapChanged(it)
    }
}

@Composable
private fun RenderPdfImage(
    bitmap: Bitmap?,
    modifier: Modifier,
) = bitmap?.run {
    Image(
        modifier = modifier,
        bitmap = asImageBitmap(),
        contentDescription = stringResource(
            R.string.cd_pdf_document
        ),
    )
}

@Composable
private inline fun RenderPdfEffect(
    assetFileName: String,
    crossinline onBitmapChanged: (Bitmap) -> Unit,
) = with(LocalContext.current) {
    val context = LocalCoroutineContext.current
    LaunchedEffect(context) {
        onBitmapChanged(
            withContext(context) {
                assets.renderPdf(
                    assetFileName,
                )
            },
        )
    }
}

private suspend fun AssetManager.renderPdf(
    assetFileName: String,
) = PdfRenderer(openFd(assetFileName)).use {
    withPtConverter {
        it.withPage(0) {
            render(
                width.pt,
                height.pt,
                RENDER_MODE_FOR_PRINT,
            )
        }
    }
}
