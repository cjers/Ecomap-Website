package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EcoMap
import com.example.ui.viewmodel.EcoMapListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EcoMapListViewModel,
    onMapSelected: (String) -> Unit
) {
    val ecoMaps by viewModel.ecoMaps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showWebDeployModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BubbleChart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EcoMap Clinical",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Psychotherapy Assessment Suite",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showWebDeployModal = true },
                        modifier = Modifier.testTag("web_deploy_info_button")
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Web Deployment (Single Session)")
                    }
                    IconButton(
                        onClick = { viewModel.restoreSampleData() },
                        modifier = Modifier.testTag("restore_sample_data_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore Sample Cases")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_new_ecomap_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Client EcoMap")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search by client name, case ID, or title...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_ecomaps_input")
            )

            if (ecoMaps.isEmpty()) {
                // Empty State Illustration Card
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Client EcoMaps Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to create your first ecomap assessment or tap restore sample cases.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.restoreSampleData() },
                            modifier = Modifier.testTag("load_sample_cases_button")
                        ) {
                            Text("Load Pre-Populated Sample Cases")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ecoMaps, key = { it.id }) { ecomap ->
                        EcoMapCard(
                            ecomap = ecomap,
                            onClick = { onMapSelected(ecomap.id) },
                            onDelete = { viewModel.deleteEcoMap(ecomap.id) }
                        )
                    }
                }
            }
        }
    }

    // Web Deployment Dialog
    if (showWebDeployModal) {
        AlertDialog(
            onDismissRequest = { showWebDeployModal = false },
            icon = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Web Deployment (Single Session Mode)") },
            text = {
                Column {
                    Text(
                        text = "A standalone single-session web edition is ready in index.html (root directory).",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Zero Persistent Data: Data exists strictly in memory for the active browser session.\n" +
                               "• Automatic Privacy Protection: Closing or refreshing the tab wipes all records.\n" +
                               "• Print/Export PDF: Clinicians can print/save PDF reports locally before closing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "To deploy on your website, copy index.html to your server root or embed it in an <iframe>.",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showWebDeployModal = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Create New Map Dialog
    if (showCreateDialog) {
        var clientName by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var caseId by remember { mutableStateOf("EC-${(1000..9999).random()}") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Client EcoMap") },
            text = {
                Column {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name / Initials") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_client_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = caseId,
                        onValueChange = { caseId = it },
                        label = { Text("Case ID / Record Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_case_id_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Assessment Title") },
                        placeholder = { Text("e.g. Single Parent Systemic Assessment") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_map_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isNotBlank()) {
                            val newId = viewModel.createNewEcoMap(clientName, title, caseId)
                            showCreateDialog = false
                            onMapSelected(newId)
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_ecomap_button")
                ) {
                    Text("Create EcoMap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EcoMapCard(
    ecomap: EcoMap,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("ecomap_card_${ecomap.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ecomap.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ecomap.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Case #${ecomap.caseId}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MetricBadge(label = "${ecomap.totalDomains} Domains", color = MaterialTheme.colorScheme.secondaryContainer)
                    Spacer(modifier = Modifier.width(6.dp))
                    MetricBadge(label = "${ecomap.nurturingCount} Nurturing", color = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(6.dp))
                    MetricBadge(label = "${ecomap.stressfulCount} Stress", color = Color(0xFFFFEBEE), textColor = Color(0xFFD32F2F))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_ecomap_button_${ecomap.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Map",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Last updated: ${ecomap.dateUpdated}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricBadge(
    label: String,
    color: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
