package com.example.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object PdfReportGenerator {

    // Standard US Letter dimensions in points (1/72 inch)
    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792

    fun generateReport(context: Context, ecomap: EcoMap): File {
        val pdfDocument = PdfDocument()

        // PAGE 1: Header, Case Info, Rendered Ecomap Diagram, Legend
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        renderPage1(page1.canvas, ecomap)
        pdfDocument.finishPage(page1)

        // PAGE 2: System Analytics Table, Clinical Impressions, Action Plan, Signatures
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        renderPage2(page2.canvas, ecomap)
        pdfDocument.finishPage(page2)

        // Save PDF to cache or documents directory
        val outputDir = File(context.cacheDir, "reports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val fileName = "EcoMap_${ecomap.caseId.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${ecomap.id.take(6)}.pdf"
        val pdfFile = File(outputDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // ------------------------------------------------------------------------
    // PAGE 1 RENDERING
    // ------------------------------------------------------------------------
    private fun renderPage1(canvas: Canvas, ecomap: EcoMap) {
        // Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Banner
        paint.color = Color.parseColor("#1A2F38")
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("ECOMAP CLINICAL ASSESSMENT REPORT", 28f, 38f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#B0BEC5")
        canvas.drawText("CONFIDENTIAL CLINICAL DOCUMENT — FOR HEALTHCARE & CASE FILES", 28f, 54f, paint)

        // Facility & Date Right-aligned
        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(ecomap.facilityName, PAGE_WIDTH - 28f, 36f, paint)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#B0BEC5")
        canvas.drawText("Date: ${ecomap.dateUpdated}", PAGE_WIDTH - 28f, 52f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Client Demographics Card
        val cardTop = 80f
        val membersStr = ecomap.householdMembers.joinToString(", ") { member ->
            val ageInfo = if (member.age.isNotBlank()) ", Age ${member.age}" else ""
            "${member.name} (${member.relation}$ageInfo)"
        }.ifEmpty { "N/A" }

        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        val memberLines = wrapTextToWidth(membersStr, 440f, paint)
        val cardHeight = 62f + (memberLines.size * 14f)

        paint.color = Color.parseColor("#F4F7F6")
        canvas.drawRoundRect(28f, cardTop, PAGE_WIDTH - 28f, cardTop + cardHeight, 8f, 8f, paint)

        paint.color = Color.parseColor("#1A2F38")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(28f, cardTop, PAGE_WIDTH - 28f, cardTop + cardHeight, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Row 1
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Client Name: ", 40f, cardTop + 20f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(ecomap.clientName, 110f, cardTop + 20f, paint)

        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Case ID: ", 240f, cardTop + 20f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(ecomap.caseId, 290f, cardTop + 20f, paint)

        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Therapist: ", 390f, cardTop + 20f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(ecomap.therapistName, 445f, cardTop + 20f, paint)

        // Row 2
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Supervisor: ", 40f, cardTop + 38f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(ecomap.supervisorName, 110f, cardTop + 38f, paint)

        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Assessment Title: ", 240f, cardTop + 38f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(ecomap.title, 340f, cardTop + 38f, paint)

        // Row 3: Household (full details, wrapped, no truncation)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Household: ", 40f, cardTop + 56f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        var memberY = cardTop + 56f
        memberLines.forEachIndexed { i, line ->
            val startX = if (i == 0) 110f else 40f
            canvas.drawText(line, startX, memberY, paint)
            if (i < memberLines.size - 1) memberY += 14f
        }

        // Diagram Box (slightly reduced height so legend sits comfortably above footer)
        val diagramTop = cardTop + cardHeight + 10f
        val diagramLeft = 28f
        val diagramWidth = PAGE_WIDTH - 56f
        val footerY = PAGE_HEIGHT - 20f
        val legendHeight = 62f
        val legendTop = footerY - legendHeight - 12f // e.g. ~698f
        val diagramHeight = legendTop - diagramTop - 10f // e.g. ~390f

        paint.color = Color.parseColor("#FAFAFA")
        canvas.drawRoundRect(diagramLeft, diagramTop, diagramLeft + diagramWidth, diagramTop + diagramHeight, 10f, 10f, paint)
        paint.color = Color.parseColor("#CFD8DC")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(diagramLeft, diagramTop, diagramLeft + diagramWidth, diagramTop + diagramHeight, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Label on top left of diagram
        paint.textSize = 9f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#546E7A")
        canvas.drawText("ECOMAP ECOLOGICAL SYSTEM DIAGRAM", diagramLeft + 12f, diagramTop + 18f, paint)

        // Central Client Household Node
        val centerX = diagramLeft + diagramWidth / 2f
        val centerY = diagramTop + diagramHeight / 2f
        val centralRadius = 45f

        // Helper calculation for Impact-based Node Distance Scaling
        // Impact 5 (High/Vital) = 0.65x distance (closer to center)
        // Impact 1 (Low/Minor) = 1.25x distance (further away on periphery)
        fun getNodeCoords(node: EcoMapNode): Pair<Float, Float> {
            val rawX = diagramLeft + (node.relativeX * diagramWidth)
            val rawY = diagramTop + (node.relativeY * diagramHeight)
            val dx = rawX - centerX
            val dy = rawY - centerY
            val distanceScale = 1.40f - (node.impactRating.coerceIn(1, 5) * 0.15f)
            val nx = (centerX + dx * distanceScale).coerceIn(diagramLeft + 35f, diagramLeft + diagramWidth - 35f)
            val ny = (centerY + dy * distanceScale).coerceIn(diagramTop + 35f, diagramTop + diagramHeight - 35f)
            return Pair(nx, ny)
        }

        // Draw connections first so nodes sit on top
        ecomap.nodes.forEach { node ->
            val (nx, ny) = getNodeCoords(node)
            val conn = ecomap.connections.find { it.targetNodeId == node.id }

            drawConnectionLine(
                canvas = canvas,
                startX = centerX,
                startY = centerY,
                endX = nx,
                endY = ny,
                centralRadius = centralRadius,
                nodeRadius = 26f,
                connectionType = conn?.connectionType ?: ConnectionType.STRONG,
                flowDirection = conn?.flowDirection ?: FlowDirection.NONE
            )
        }

        // Draw Central Node
        paint.color = Color.parseColor("#1A2F38")
        canvas.drawCircle(centerX, centerY, centralRadius, paint)
        paint.color = Color.parseColor("#00796B")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(centerX, centerY, centralRadius, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("CLIENT /", centerX, centerY - 15f, paint)
        canvas.drawText("HOUSEHOLD", centerX, centerY - 5f, paint)

        paint.textSize = 8f
        paint.isFakeBoldText = false
        val firstName = ecomap.clientName.split(" ").firstOrNull() ?: ecomap.clientName
        canvas.drawText(firstName, centerX, centerY + 6f, paint)

        paint.textSize = 7f
        paint.color = Color.parseColor("#B0BEC5")
        canvas.drawText("${ecomap.householdMembers.size} Member(s)", centerX, centerY + 18f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Draw Surrounding Domain Nodes (positioned by Impact scale)
        ecomap.nodes.forEach { node ->
            val (nx, ny) = getNodeCoords(node)
            val nodeRadius = 26f

            // Fill
            paint.color = Color.parseColor(toHex(node.category.defaultColor))
            canvas.drawCircle(nx, ny, nodeRadius, paint)

            // Outer border
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawCircle(nx, ny, nodeRadius, paint)
            paint.style = Paint.Style.FILL

            // Category abbreviation
            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            val categoryAbbr = node.category.name.take(3)
            canvas.drawText(categoryAbbr, nx, ny + 3f, paint)

            // Node Title underneath
            paint.color = Color.parseColor("#263238")
            paint.textSize = 8f
            paint.isFakeBoldText = true
            val titleLines = wrapText(node.title, 18)
            var lineY = ny + nodeRadius + 10f
            titleLines.take(2).forEach { line ->
                canvas.drawText(line, nx, lineY, paint)
                lineY += 9f
            }
            paint.textAlign = Paint.Align.LEFT
        }

        // Legend Box
        paint.color = Color.parseColor("#F4F7F6")
        canvas.drawRoundRect(28f, legendTop, PAGE_WIDTH - 28f, legendTop + legendHeight, 6f, 6f, paint)
        paint.color = Color.parseColor("#CFD8DC")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(28f, legendTop, PAGE_WIDTH - 28f, legendTop + legendHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#1A2F38")
        canvas.drawText("RELATIONAL DYNAMICS LEGEND", 40f, legendTop + 16f, paint)

        // Key items
        drawLegendItem(canvas, 40f, legendTop + 32f, "Strong / Nurturing", Color.parseColor("#2E7D32"), false)
        drawLegendItem(canvas, 180f, legendTop + 32f, "Stressful / Conflict", Color.parseColor("#D32F2F"), true)
        drawLegendItem(canvas, 330f, legendTop + 32f, "Tenuous / Weak", Color.parseColor("#F57C00"), false)

        drawLegendItem(canvas, 40f, legendTop + 48f, "Energy Giving (Inflow)", Color.parseColor("#0288D1"), false)
        drawLegendItem(canvas, 180f, legendTop + 48f, "Energy Draining (Outflow)", Color.parseColor("#C2185B"), true)
        drawLegendItem(canvas, 330f, legendTop + 48f, "Reciprocal Exchange", Color.parseColor("#7B1FA2"), false)

        // Footer
        paint.textSize = 8f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#78909C")
        canvas.drawText("EcoMap Clinical System • Page 1 of 2", 28f, footerY, paint)
    }

    // ------------------------------------------------------------------------
    // PAGE 2 RENDERING
    // ------------------------------------------------------------------------
    private fun renderPage2(canvas: Canvas, ecomap: EcoMap) {
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Banner
        paint.color = Color.parseColor("#1A2F38")
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 50f, paint)

        paint.color = Color.WHITE
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("CLINICAL ASSESSMENT & SYSTEMIC ANALYSIS", 28f, 32f, paint)

        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Case ID: ${ecomap.caseId} | Client: ${ecomap.clientName}", PAGE_WIDTH - 28f, 32f, paint)
        paint.textAlign = Paint.Align.LEFT

        var topY = 66f

        // System Metrics Bar
        paint.color = Color.parseColor("#E0F2F1")
        canvas.drawRoundRect(28f, topY, PAGE_WIDTH - 28f, topY + 44f, 6f, 6f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#004D40")
        canvas.drawText("Total Domains: ${ecomap.totalDomains}", 40f, topY + 26f, paint)
        canvas.drawText("Nurturing Assets: ${ecomap.nurturingCount}", 160f, topY + 26f, paint)
        canvas.drawText("Stress Vectors: ${ecomap.stressfulCount}", 300f, topY + 26f, paint)
        canvas.drawText("Tenuous Links: ${ecomap.tenuousCount}", 430f, topY + 26f, paint)

        topY += 56f

        // Table Header: Ecological Domains Breakdown
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#1A2F38")
        canvas.drawText("ECOLOGICAL DOMAIN ASSESSMENT & RELATIONAL DETAILS", 28f, topY, paint)
        topY += 12f

        // Table Column Headers
        paint.color = Color.parseColor("#263740")
        canvas.drawRect(28f, topY, PAGE_WIDTH - 28f, topY + 20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("Domain Title", 34f, topY + 14f, paint)
        canvas.drawText("Category", 155f, topY + 14f, paint)
        canvas.drawText("Dynamics / Flow", 245f, topY + 14f, paint)
        canvas.drawText("Impact", 340f, topY + 14f, paint)
        canvas.drawText("Clinical Notes", 385f, topY + 14f, paint)

        topY += 20f

        // Table Rows (renders ALL domains with full multi-line word-wrapped Clinical Notes)
        paint.isFakeBoldText = false
        val tableFontSize = if (ecomap.nodes.size <= 8) 8f else 7.5f
        paint.textSize = tableFontSize

        ecomap.nodes.forEachIndexed { idx, node ->
            val conn = ecomap.connections.find { it.targetNodeId == node.id }
            val notesTxt = if (node.notes.isNotBlank()) node.notes else conn?.notes ?: "None"
            val noteLines = wrapTextToWidth(notesTxt, 185f, paint)
            
            val rowHeight = maxOf(20f, noteLines.size * 10.5f + 6f)
            val textOffsetY = 13f

            val rowBg = if (idx % 2 == 0) Color.parseColor("#FFFFFF") else Color.parseColor("#F4F7F6")
            paint.color = rowBg
            canvas.drawRect(28f, topY, PAGE_WIDTH - 28f, topY + rowHeight, paint)

            paint.color = Color.parseColor("#CFD8DC")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRect(28f, topY, PAGE_WIDTH - 28f, topY + rowHeight, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#263238")
            canvas.drawText(truncateText(node.title, 20), 34f, topY + textOffsetY, paint)

            paint.color = Color.parseColor(toHex(node.category.defaultColor))
            paint.isFakeBoldText = true
            canvas.drawText(node.category.displayName.take(14), 155f, topY + textOffsetY, paint)

            paint.isFakeBoldText = false
            paint.color = Color.parseColor(toHex(conn?.connectionType?.color ?: androidx.compose.ui.graphics.Color.Gray))
            val flowDesc = conn?.connectionType?.displayName ?: "Unspecified"
            canvas.drawText(truncateText(flowDesc, 16), 245f, topY + textOffsetY, paint)

            paint.color = Color.parseColor("#37474F")
            canvas.drawText("${node.impactRating}/5", 340f, topY + textOffsetY, paint)

            // Multi-line word wrapped Clinical Notes column (all notes displayed)
            paint.color = Color.parseColor("#263238")
            var lineY = topY + textOffsetY
            noteLines.forEach { line ->
                canvas.drawText(line, 385f, lineY, paint)
                lineY += 10f
            }

            topY += rowHeight
        }

        topY += 12f

        // Presenting Context & Clinical Impression Box
        val formulationHeight = if (ecomap.nodes.size > 8) 100f else 125f
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#1A2F38")
        canvas.drawText("THERAPIST CLINICAL FORMULATION & SYSTEM DYNAMICS", 28f, topY, paint)
        topY += 6f

        paint.color = Color.parseColor("#FAFAFA")
        canvas.drawRoundRect(28f, topY, PAGE_WIDTH - 28f, topY + formulationHeight, 6f, 6f, paint)
        paint.color = Color.parseColor("#CFD8DC")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(28f, topY, PAGE_WIDTH - 28f, topY + formulationHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        canvas.drawText("Presenting Problem & Ecological Context:", 38f, topY + 16f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#263238")
        val ctxText = ecomap.presentingContext.ifBlank { "Client presents for clinical ecomap assessment of household and environmental stress vectors." }
        drawParagraph(canvas, ctxText, 38f, topY + 28f, PAGE_WIDTH - 76f, 10f, paint, 3)

        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#00796B")
        val obsOffset = if (ecomap.nodes.size > 8) 56f else 66f
        canvas.drawText("Therapist Observations & Systemic Assets:", 38f, topY + obsOffset, paint)
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#263238")
        val obsText = ecomap.clinicalImpressions.ifBlank { "High protective resiliency observed in informal support networks. Primary friction identified in institutional domain." }
        drawParagraph(canvas, obsText, 38f, topY + obsOffset + 12f, PAGE_WIDTH - 76f, 10f, paint, 3)

        // Signatures Block
        val sigTop = PAGE_HEIGHT - 110f
        paint.color = Color.parseColor("#ECEFF1")
        canvas.drawRect(28f, sigTop, PAGE_WIDTH - 28f, sigTop + 70f, paint)

        paint.color = Color.parseColor("#37474F")
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("CLINICAL SIGN-OFF & SUPERVISORY REVIEW", 38f, sigTop + 18f, paint)

        // Therapist Line
        paint.color = Color.parseColor("#78909C")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(38f, sigTop + 46f, 260f, sigTop + 46f, paint)
        canvas.drawLine(310f, sigTop + 46f, 550f, sigTop + 46f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#263238")
        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText("Primary Therapist Signature / Credentials", 38f, sigTop + 58f, paint)
        canvas.drawText("Clinical Supervisor Signature & Date", 310f, sigTop + 58f, paint)

        // Footer
        paint.textSize = 8f
        paint.color = Color.parseColor("#78909C")
        canvas.drawText("EcoMap Clinical System • Page 2 of 2", 28f, PAGE_HEIGHT - 20f, paint)
    }

    // Helper functions for canvas rendering
    private fun drawConnectionLine(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        centralRadius: Float,
        nodeRadius: Float,
        connectionType: ConnectionType,
        flowDirection: FlowDirection
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor(toHex(connectionType.color))
        paint.strokeWidth = if (connectionType == ConnectionType.STRONG) 3.5f else 2.5f

        // Trim start and end points to node edges
        val dx = endX - startX
        val dy = endY - startY
        val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (dist <= centralRadius + nodeRadius) return

        val sx = startX + (dx * centralRadius / dist)
        val sy = startY + (dy * centralRadius / dist)
        val ex = endX - (dx * nodeRadius / dist)
        val ey = endY - (dy * nodeRadius / dist)

        if (connectionType == ConnectionType.STRESSFUL) {
            // Draw zig-zag line
            val path = Path()
            path.moveTo(sx, sy)
            val steps = 8
            val stepX = (ex - sx) / steps
            val stepY = (ey - sy) / steps
            val perpX = -(ey - sy) / dist * 10f
            val perpY = (ex - sx) / dist * 10f

            for (i in 1..steps) {
                val px = sx + stepX * i + if (i % 2 == 1) perpX else -perpX
                val py = sy + stepY * i + if (i % 2 == 1) perpY else -perpY
                path.lineTo(px, py)
            }
            paint.style = Paint.Style.STROKE
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
        } else if (connectionType == ConnectionType.TENUOUS || connectionType == ConnectionType.DRAINING) {
            // Dashed line
            paint.style = Paint.Style.STROKE
            paint.pathEffect = DashPathEffect(floatArrayOf(10f, 6f), 0f)
            canvas.drawLine(sx, sy, ex, ey, paint)
            paint.pathEffect = null
            paint.style = Paint.Style.FILL
        } else {
            // Solid line
            paint.style = Paint.Style.STROKE
            canvas.drawLine(sx, sy, ex, ey, paint)
            paint.style = Paint.Style.FILL
        }

        // Draw Flow Arrows
        val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
        val arrowSize = 10f

        if (flowDirection == FlowDirection.TOWARDS_CLIENT || flowDirection == FlowDirection.BIDIRECTIONAL) {
            // Arrow pointing to client (at sx, sy)
            drawArrowhead(canvas, sx, sy, angle + Math.PI.toFloat(), arrowSize, paint.color)
        }
        if (flowDirection == FlowDirection.AWAY_FROM_CLIENT || flowDirection == FlowDirection.BIDIRECTIONAL) {
            // Arrow pointing away to domain (at ex, ey)
            drawArrowhead(canvas, ex, ey, angle, arrowSize, paint.color)
        }
    }

    private fun drawArrowhead(canvas: Canvas, tipX: Float, tipY: Float, angle: Float, size: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.style = Paint.Style.FILL

        val path = Path()
        path.moveTo(tipX, tipY)
        path.lineTo(
            (tipX - size * cos(angle - Math.PI / 6)).toFloat(),
            (tipY - size * sin(angle - Math.PI / 6)).toFloat()
        )
        path.lineTo(
            (tipX - size * cos(angle + Math.PI / 6)).toFloat(),
            (tipY - size * sin(angle + Math.PI / 6)).toFloat()
        )
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawLegendItem(canvas: Canvas, x: Float, y: Float, label: String, color: Int, isDashed: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.strokeWidth = 2.5f

        if (isDashed) {
            paint.style = Paint.Style.STROKE
            paint.pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
            canvas.drawLine(x, y - 3f, x + 24f, y - 3f, paint)
            paint.pathEffect = null
            paint.style = Paint.Style.FILL
        } else {
            paint.style = Paint.Style.STROKE
            canvas.drawLine(x, y - 3f, x + 24f, y - 3f, paint)
            paint.style = Paint.Style.FILL
        }

        paint.textSize = 8f
        paint.color = Color.parseColor("#37474F")
        canvas.drawText(label, x + 30f, y, paint)
    }

    private fun drawParagraph(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, lineHeight: Float, paint: Paint, maxLines: Int) {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y
        var linesDrawn = 0

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, x, currentY, paint)
                currentLine = word
                currentY += lineHeight
                linesDrawn++
                if (linesDrawn >= maxLines - 1) break
            }
        }
        if (currentLine.isNotEmpty() && linesDrawn < maxLines) {
            canvas.drawText(truncateText(currentLine, (maxWidth / 5).toInt()), x, currentY, paint)
        }
    }

    private fun truncateText(text: String, maxChars: Int): String {
        return if (text.length > maxChars) text.take(maxChars - 2) + "…" else text
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            if (currentLine.isEmpty()) {
                currentLine = word
            } else if (currentLine.length + word.length + 1 <= maxCharsPerLine) {
                currentLine += " $word"
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    private fun wrapTextToWidth(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isBlank()) return listOf("None")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines.ifEmpty { listOf(text) }
    }

    private fun toHex(color: androidx.compose.ui.graphics.Color): String {
        val alpha = (color.alpha * 255).toInt()
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    }
}
