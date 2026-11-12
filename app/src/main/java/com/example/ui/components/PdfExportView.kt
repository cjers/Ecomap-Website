package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EcoMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Composable
fun PdfExportView(
    ecomap: EcoMap,
    pdfFile: File?,
    pdfUri: Uri?,
    isGenerating: Boolean,
    onGeneratePdfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Auto-trigger PDF generation if null or for a different case/ecomap
    LaunchedEffect(ecomap.id, ecomap.dateUpdated, pdfFile) {
        val isStaleOrNull = pdfFile == null || !pdfFile.name.contains(ecomap.id.take(6))
        if (isStaleOrNull && !isGenerating) {
            onGeneratePdfClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pdf_export_view")
    ) {
        // Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (pdfFile != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (pdfFile != null) "Clinical Report Ready" else "Generating Professional PDF...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (pdfFile != null) "2-Page Vector Assessment & Diagram (${pdfFile.length() / 1024} KB)" else "Formatting header, ecomap diagram & sign-offs...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = onGeneratePdfClick,
                        modifier = Modifier.testTag("refresh_pdf_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh PDF")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        if (pdfFile != null && pdfUri != null) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { sharePdf(context, pdfUri) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .testTag("share_pdf_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Report")
                }

                OutlinedButton(
                    onClick = { savePdfToDownloads(context, pdfFile, ecomap.caseId) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                        .testTag("save_pdf_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save File")
                }
            }
        }
    }
}

private fun sharePdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share EcoMap Clinical Report"))
}

private fun savePdfToDownloads(context: Context, pdfFile: File, caseId: String) {
    val cleanCaseId = caseId.replace("[^a-zA-Z0-9]".toRegex(), "_")
    val fileName = "EcoMap_Report_${cleanCaseId}.pdf"

    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(pdfFile).use { input ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "Saved PDF to Downloads (${fileName})", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Fallback for older Android versions
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val destFile = File(downloadsDir, fileName)

        FileInputStream(pdfFile).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        Toast.makeText(context, "Saved PDF to Downloads (${destFile.name})", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Saved PDF report: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
