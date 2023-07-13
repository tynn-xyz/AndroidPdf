//  Copyright 2023 Christian Schmitz
//  SPDX-License-Identifier: Apache-2.0

package xyz.tynn.android.pdf.example

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.tynn.android.pdf.example.SharePdfMode.Pdf
import xyz.tynn.android.pdf.example.SharePdfMode.Png
import xyz.tynn.android.pdf.example.SharePdfMode.Raw

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalCoroutineContext + PtConverter()) {
                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                Scaffold(
                    content = { padding ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(padding),
                            horizontalAlignment = CenterHorizontally,
                        ) {
                            RenderPdf(
                                Modifier.padding(
                                    horizontal = 32.dp,
                                    vertical = 96.dp,
                                ),
                            ) { bitmap = it }
                        }
                    },
                    bottomBar = {
                        Row(Modifier.fillMaxWidth(), Center) {
                            SharePdf(Raw, bitmap)
                            SharePdf(Png, bitmap)
                            SharePdf(Pdf, bitmap)
                        }
                    },
                )
            }
        }
    }
}
