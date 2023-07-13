//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.net.Uri
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.content.FileProvider.getUriForFile
import androidx.print.PrintHelper
import androidx.print.PrintHelper.SCALE_MODE_FIT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import xyz.tynn.android.pdf.example.SharePdfMode.Pdf
import xyz.tynn.android.pdf.example.SharePdfMode.Png
import xyz.tynn.android.pdf.example.SharePdfMode.Raw
import xyz.tynn.android.pdf.ktx.PageInfo
import xyz.tynn.android.pdf.ktx.addPage
import xyz.tynn.android.pdf.ktx.use
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.webkit.MimeTypeMap.getSingleton as getMimeTypeMap

enum class SharePdfMode(@StringRes val textRes: Int) {
    Raw(R.string.btn_print_pdf),
    Png(R.string.btn_share_png),
    Pdf(R.string.btn_share_pdf),
}

@Composable
fun SharePdf(
    mode: SharePdfMode,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
) = with(LocalContext.current) {
    SharePdfButton(mode.textRes, modifier, bitmap != null) {
        sharePdf(mode, bitmap!!)
    }
}

@Composable
private inline fun SharePdfButton(
    @StringRes textRes: Int,
    modifier: Modifier,
    enabled: Boolean,
    crossinline onClick: suspend () -> Unit,
) = with(rememberLocalCoroutineScope()) {
    TextButton(
        modifier = modifier,
        enabled = enabled,
        content = { Text(stringResource(textRes)) },
        onClick = { launch { onClick() } },
    )
}

@Composable
private fun rememberLocalCoroutineScope(): CoroutineScope {
    val context = LocalCoroutineContext.current
    return rememberCoroutineScope { context }
}

private suspend fun Context.sharePdf(
    mode: SharePdfMode,
    bitmap: Bitmap,
) = when (mode) {
    Raw -> printPdf(bitmap)
    Png -> sharePng(bitmap)
    Pdf -> sharePdf(bitmap)
}

private suspend fun Context.printPdf(bitmap: Bitmap) {
    suspendCoroutine {
        PrintHelper(this).apply {
            scaleMode = SCALE_MODE_FIT
        }.printBitmap("AndroidPdf", bitmap) {
            it.resume(null)
        }
    }
}

private suspend fun Context.sharePng(bitmap: Bitmap) {
    shareFile("android.png") {
        runInterruptible {
            bitmap.compress(PNG, 100, it)
        }
    }
}

private suspend fun Context.sharePdf(bitmap: Bitmap) {
    bitmap.toPdfDocument().use { pdf ->
        shareFile("android.pdf") {
            runInterruptible {
                pdf.writeTo(it)
            }
        }
    }
}

private suspend fun Bitmap.toPdfDocument() = withPtConverter {
    PdfDocument().apply {
        addPage(
            PageInfo(
                width.px,
                height.px,
                pageNumber = 0,
            ),
            draw = ::drawInto,
        )
    }
}

private fun Bitmap.drawInto(
    page: Page,
) = page.canvas.drawBitmap(
    this,
    null,
    page.info.contentRect,
    null,
)

private inline fun Context.shareFile(
    fileName: String,
    storeData: (FileOutputStream) -> Unit,
) = IntentBuilder(this).setStreamAndType(
    getUriForFile(
        this,
        "$packageName.share",
        File(cacheDir, fileName).apply {
            outputStream().use {
                storeData(it)
            }
        },
    )
).startChooser()

private fun IntentBuilder.setStreamAndType(uri: Uri) = apply {
    setStream(uri)
    setType(uri.mimeType)
}

private val Uri.mimeType
    get() = getMimeTypeMap().getMimeTypeFromExtension(
        getFileExtensionFromUrl(path),
    )
