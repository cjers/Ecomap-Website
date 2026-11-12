package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionType
import com.example.data.model.EcoMap

@Composable
fun SystemAnalyticsView(
    ecomap: EcoMap,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("system_analytics_view")
    ) {
        Text(
            text = "Ecological System Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Systemic Balance Row Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            // Nurturing Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ecomap.nurturingCount}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Nurturing\nAssets",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Stressors Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ecomap.stressfulCount}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Text(
                        text = "Stress\nVectors",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // Tenuous Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFFF57C00))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ecomap.tenuousCount}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "Tenuous\nLinks",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFF57C00)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categorized Systemic Domains List
        Text(
            text = "Systemic Domain Impact & Dynamic Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (ecomap.nodes.isEmpty()) {
            Text(text = "No domain nodes added yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            ecomap.nodes.forEach { node ->
                val conn = ecomap.connections.find { it.targetNodeId == node.id }
                val connType = conn?.connectionType ?: ConnectionType.STRONG

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = node.category.defaultColor,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = node.category.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = node.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${node.category.displayName} • ${connType.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = connType.color,
                                fontWeight = FontWeight.Bold
                            )
                            if (node.notes.isNotBlank()) {
                                Text(
                                    text = node.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Rating badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "Impact: ${node.impactRating}/5",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
