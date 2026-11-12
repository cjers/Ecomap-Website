package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*

@Composable
fun AddEditNodeDialog(
    initialNode: EcoMapNode? = null,
    initialConnection: EcoMapConnection? = null,
    onDismiss: () -> Unit,
    onSave: (EcoMapNode, ConnectionType?, FlowDirection?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialNode?.title ?: "") }
    var category by remember { mutableStateOf(initialNode?.category ?: DomainCategory.WORK) }
    var impactRating by remember { mutableFloatStateOf((initialNode?.impactRating ?: 3).toFloat()) }
    var notes by remember { mutableStateOf(initialNode?.notes ?: "") }

    var selectedConnectionType by remember {
        mutableStateOf(initialConnection?.connectionType ?: ConnectionType.STRONG)
    }
    var selectedFlowDirection by remember {
        mutableStateOf(initialConnection?.flowDirection ?: selectedConnectionType.defaultFlow)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_edit_node_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialNode == null) "Add Ecological Domain" else "Edit Domain Node",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Domain Title (e.g. St. Jude School, Work)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("domain_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Grid Selection
                Text(
                    text = "Select Domain Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .height(130.dp)
                        .fillMaxWidth()
                ) {
                    items(DomainCategory.entries) { item ->
                        val isSelected = category == item
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) item.defaultColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) item.defaultColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { category = item }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.displayName,
                                    tint = item.defaultColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.displayName.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Impact Rating Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Domain Impact Rating: ${impactRating.toInt()}/5", style = MaterialTheme.typography.labelLarge)
                    Row {
                        repeat(5) { idx ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (idx < impactRating.toInt()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Slider(
                    value = impactRating,
                    onValueChange = { impactRating = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.testTag("impact_rating_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Connection Type Selector
                Text(text = "Relational Dynamic", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(90.dp)
                ) {
                    items(ConnectionType.entries) { type ->
                        val isSelected = selectedConnectionType == type
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) type.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(type.color)) else null,
                            modifier = Modifier
                                .padding(3.dp)
                                .clickable {
                                    selectedConnectionType = type
                                    selectedFlowDirection = type.defaultFlow
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(type.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = type.displayName.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes / Context") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("node_notes_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_node_button")
                        ) {
                            Text("Delete")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val node = initialNode?.copy(
                                    title = title,
                                    category = category,
                                    impactRating = impactRating.toInt(),
                                    notes = notes
                                ) ?: EcoMapNode(
                                    ecomapId = "",
                                    title = title,
                                    category = category,
                                    impactRating = impactRating.toInt(),
                                    notes = notes,
                                    relativeX = 0.5f + ((1..4).random() * 0.08f * if ((1..2).random() == 1) 1 else -1),
                                    relativeY = 0.5f + ((1..4).random() * 0.08f * if ((1..2).random() == 1) 1 else -1)
                                )
                                onSave(node, selectedConnectionType, selectedFlowDirection)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_node_button")
                    ) {
                        Text("Save Domain")
                    }
                }
            }
        }
    }
}
