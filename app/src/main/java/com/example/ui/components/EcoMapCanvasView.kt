package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.SlatePrimary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EcoMapCanvasView(
    ecomap: EcoMap,
    selectedNodeId: String?,
    onNodeSelected: (EcoMapNode) -> Unit,
    onNodeDrag: (String, Float, Float) -> Unit,
    onAddNodeClick: () -> Unit,
    onCircleLayoutClick: () -> Unit,
    onSymmetricLayoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLegend by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.4f, 2.5f)
                    panOffset += pan
                }
            }
            .testTag("ecomap_canvas_view")
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { constraints.maxWidth.toDp().toPx() }
        val heightPx = with(density) { constraints.maxHeight.toDp().toPx() }

        val centerX = widthPx * 0.5f
        val centerY = heightPx * 0.5f
        val centralRadiusPx = with(density) { 54.dp.toPx() }
        val nodeRadiusPx = with(density) { 32.dp.toPx() }

        // Scalable and Pannable Canvas Container Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                }
        ) {
            // 1. Canvas Layer: Draw Connections & Dotted Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Subtle Dotted Grid Background
                val gridSpacing = 40.dp.toPx()
                var x = 0f
                while (x < widthPx) {
                    var y = 0f
                    while (y < heightPx) {
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.15f),
                            radius = 1.5f,
                            center = Offset(x, y)
                        )
                        y += gridSpacing
                    }
                    x += gridSpacing
                }

                // Draw Connections between Center & Domain Nodes
                ecomap.nodes.forEach { node ->
                    val nx = (node.relativeX * widthPx).coerceIn(40f, widthPx - 40f)
                    val ny = (node.relativeY * heightPx).coerceIn(40f, heightPx - 40f)
                    val conn = ecomap.connections.find { it.targetNodeId == node.id }
                    val connType = conn?.connectionType ?: ConnectionType.STRONG
                    val flow = conn?.flowDirection ?: FlowDirection.NONE

                    drawRelationalConnection(
                        startX = centerX,
                        startY = centerY,
                        endX = nx,
                        endY = ny,
                        startRadius = centralRadiusPx,
                        endRadius = nodeRadiusPx,
                        connectionType = connType,
                        flowDirection = flow
                    )
                }
            }

            // 2. Central Client Household Node Composable
            val centerDpX = with(density) { (centerX - centralRadiusPx).toDp() }
            val centerDpY = with(density) { (centerY - centralRadiusPx).toDp() }

            Surface(
                shape = CircleShape,
                color = SlatePrimary,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .offset(x = centerDpX, y = centerDpY)
                    .size(108.dp)
                    .testTag("central_client_node")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Household",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ecomap.clientName.split(" ").firstOrNull() ?: "Client",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${ecomap.householdMembers.size} Member(s)",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }

            // 3. Surrounding Domain Nodes Composables (Draggable)
            ecomap.nodes.forEach { node ->
                val nodeX = (node.relativeX * widthPx).coerceIn(40f, widthPx - 40f)
                val nodeY = (node.relativeY * heightPx).coerceIn(40f, heightPx - 40f)
                val isSelected = selectedNodeId == node.id

                val nodeDpX = with(density) { (nodeX - nodeRadiusPx).toDp() }
                val nodeDpY = with(density) { (nodeY - nodeRadiusPx).toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = nodeDpX, y = nodeDpY)
                        .pointerInput(node.id, zoomScale) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newX = (nodeX + dragAmount.x / zoomScale) / widthPx
                                val newY = (nodeY + dragAmount.y / zoomScale) / heightPx
                                onNodeDrag(node.id, newX, newY)
                            }
                        }
                        .testTag("domain_node_${node.id}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(96.dp)
                    ) {
                        // Node Circle
                        Surface(
                            shape = CircleShape,
                            color = node.category.defaultColor,
                            shadowElevation = if (isSelected) 10.dp else 4.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 3.5.dp else 1.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White
                            ),
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = node.category.icon,
                                    contentDescription = node.category.displayName,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Title Badge Card underneath node
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = 2.dp,
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = node.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(node.impactRating.coerceIn(1, 5)) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(9.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Edit Button floating overlay
                    IconButton(
                        onClick = { onNodeSelected(node) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("node_edit_badge_${node.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Node",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // 4. Floating Zoom & Layout Controls Toolbar
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Zoom In
            FilledTonalIconButton(
                onClick = { zoomScale = (zoomScale + 0.15f).coerceIn(0.4f, 2.5f) },
                modifier = Modifier.testTag("zoom_in_button")
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Zoom Out
            FilledTonalIconButton(
                onClick = { zoomScale = (zoomScale - 0.15f).coerceIn(0.4f, 2.5f) },
                modifier = Modifier.testTag("zoom_out_button")
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Reset Zoom % Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    }
                    .testTag("reset_zoom_button")
            ) {
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Circle Layout
            FilledTonalIconButton(
                onClick = onCircleLayoutClick,
                modifier = Modifier.testTag("circle_layout_button")
            ) {
                Icon(Icons.Default.Adjust, contentDescription = "Circle Layout")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Grid Layout
            FilledTonalIconButton(
                onClick = onSymmetricLayoutClick,
                modifier = Modifier.testTag("symmetric_layout_button")
            ) {
                Icon(Icons.Default.ViewAgenda, contentDescription = "Grid Layout")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Toggle Legend
            FilledTonalIconButton(
                onClick = { showLegend = !showLegend },
                modifier = Modifier.testTag("toggle_legend_button")
            ) {
                Icon(
                    imageVector = if (showLegend) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle Legend"
                )
            }
        }

        // Add Node Floating Action Button
        ExtendedFloatingActionButton(
            onClick = onAddNodeClick,
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Domain") },
            text = { Text("Add Domain") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_domain_fab")
        )

        // 5. Relational Dynamics Legend Drawer Overlay
        if (showLegend) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .width(220.dp)
                    .testTag("ecomap_legend_card")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Relational Dynamics Key",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LegendRow("Strong / Nurturing", ConnectionType.STRONG.color, isDashed = false)
                    LegendRow("Stressful / Conflict", ConnectionType.STRESSFUL.color, isDashed = true)
                    LegendRow("Tenuous / Weak", ConnectionType.TENUOUS.color, isDashed = true)
                    LegendRow("Energy Giving", ConnectionType.GIVING.color, isDashed = false)
                    LegendRow("Energy Draining", ConnectionType.DRAINING.color, isDashed = true)
                    LegendRow("Reciprocal Support", ConnectionType.RECIPROCAL.color, isDashed = false)
                }
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, color: Color, isDashed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRelationalConnection(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    startRadius: Float,
    endRadius: Float,
    connectionType: ConnectionType,
    flowDirection: FlowDirection
) {
    val dx = endX - startX
    val dy = endY - startY
    val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
    if (dist <= startRadius + endRadius) return

    val sx = startX + (dx * startRadius / dist)
    val sy = startY + (dy * startRadius / dist)
    val ex = endX - (dx * endRadius / dist)
    val ey = endY - (dy * endRadius / dist)

    val strokeWidthPx = if (connectionType == ConnectionType.STRONG) 8f else 5f
    val lineColor = connectionType.color

    when (connectionType) {
        ConnectionType.STRESSFUL -> {
            // Jagged zig-zag line
            val path = Path()
            path.moveTo(sx, sy)
            val steps = 7
            val stepX = (ex - sx) / steps
            val stepY = (ey - sy) / steps
            val perpX = -(ey - sy) / dist * 12f
            val perpY = (ex - sx) / dist * 12f

            for (i in 1..steps) {
                val px = sx + stepX * i + if (i % 2 == 1) perpX else -perpX
                val py = sy + stepY * i + if (i % 2 == 1) perpY else -perpY
                path.lineTo(px, py)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = strokeWidthPx)
            )
        }
        ConnectionType.TENUOUS, ConnectionType.DRAINING -> {
            // Dashed line
            drawLine(
                color = lineColor,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = strokeWidthPx,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
            )
        }
        else -> {
            // Solid line
            drawLine(
                color = lineColor,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = strokeWidthPx
            )
        }
    }

    // Draw flow arrowheads
    val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
    val arrowSize = 14f

    if (flowDirection == FlowDirection.TOWARDS_CLIENT || flowDirection == FlowDirection.BIDIRECTIONAL) {
        drawArrow(sx, sy, angle + Math.PI.toFloat(), arrowSize, lineColor)
    }
    if (flowDirection == FlowDirection.AWAY_FROM_CLIENT || flowDirection == FlowDirection.BIDIRECTIONAL) {
        drawArrow(ex, ey, angle, arrowSize, lineColor)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    tipX: Float,
    tipY: Float,
    angle: Float,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(tipX, tipY)
        lineTo(
            (tipX - size * cos(angle - Math.PI / 6)).toFloat(),
            (tipY - size * sin(angle - Math.PI / 6)).toFloat()
        )
        lineTo(
            (tipX - size * cos(angle + Math.PI / 6)).toFloat(),
            (tipY - size * sin(angle + Math.PI / 6)).toFloat()
        )
        close()
    }
    drawPath(path = path, color = color)
}
