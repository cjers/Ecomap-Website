package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EcoMapNode
import com.example.ui.components.*
import com.example.ui.viewmodel.DetailTab
import com.example.ui.viewmodel.EcoMapDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoMapDetailScreen(
    ecomapId: String,
    viewModel: EcoMapDetailViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(ecomapId) {
        viewModel.loadEcoMap(ecomapId)
    }

    val currentMap by viewModel.currentMap.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()
    val pdfUri by viewModel.pdfUri.collectAsState()

    var showAddNodeDialog by remember { mutableStateOf(false) }
    var editingNode by remember { mutableStateOf<EcoMapNode?>(null) }

    val ecomap = currentMap

    if (ecomap == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ecomap.clientName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Case #${ecomap.caseId} • ${ecomap.title}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.applyCircularLayout() },
                        modifier = Modifier.testTag("circle_layout_action")
                    ) {
                        Icon(Icons.Default.Adjust, contentDescription = "Circle Auto-Layout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("detail_tab_row")
            ) {
                DetailTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            // Tab Content Switcher
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    DetailTab.CANVAS -> {
                        EcoMapCanvasView(
                            ecomap = ecomap,
                            selectedNodeId = selectedNodeId,
                            onNodeSelected = { node ->
                                viewModel.selectNode(node.id)
                                editingNode = node
                                showAddNodeDialog = true
                            },
                            onNodeDrag = { nodeId, newX, newY ->
                                viewModel.updateNodePosition(nodeId, newX, newY)
                            },
                            onAddNodeClick = {
                                editingNode = null
                                showAddNodeDialog = true
                            },
                            onCircleLayoutClick = { viewModel.applyCircularLayout() },
                            onSymmetricLayoutClick = { viewModel.applySymmetricCategoryLayout() }
                        )
                    }
                    DetailTab.NOTES -> {
                        ClinicalNotesView(
                            ecomap = ecomap,
                            onUpdateClientDetails = viewModel::updateClientDetails,
                            onUpdateClinicalNotes = { ctx, obs -> viewModel.updateClinicalNotes(ctx, obs) }
                        )
                    }
                    DetailTab.ANALYTICS -> {
                        SystemAnalyticsView(ecomap = ecomap)
                    }
                    DetailTab.PDF_REPORT -> {
                        PdfExportView(
                            ecomap = ecomap,
                            pdfFile = pdfFile,
                            pdfUri = pdfUri,
                            isGenerating = isGeneratingPdf,
                            onGeneratePdfClick = { viewModel.generatePdfReport(context) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Node Dialog
    if (showAddNodeDialog) {
        val activeConn = editingNode?.let { node -> ecomap.connections.find { it.targetNodeId == node.id } }

        AddEditNodeDialog(
            initialNode = editingNode,
            initialConnection = activeConn,
            onDismiss = {
                showAddNodeDialog = false
                editingNode = null
            },
            onSave = { node, connType, flowDir ->
                viewModel.addOrUpdateNode(node, connType, flowDir)
                showAddNodeDialog = false
                editingNode = null
                Toast.makeText(context, "Saved domain node", Toast.LENGTH_SHORT).show()
            },
            onDelete = if (editingNode != null) {
                {
                    editingNode?.id?.let { viewModel.deleteNode(it) }
                    showAddNodeDialog = false
                    editingNode = null
                }
            } else null
        )
    }
}
