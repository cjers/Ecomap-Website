package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.EcoMapRepository
import com.example.pdf.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

enum class DetailTab(val label: String) {
    CANVAS("Interactive Canvas"),
    NOTES("Clinical Case Notes"),
    ANALYTICS("System Analytics"),
    PDF_REPORT("PDF Clinical Report")
}

class EcoMapDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EcoMapRepository(application)

    private val _currentMap = MutableStateFlow<EcoMap?>(null)
    val currentMap: StateFlow<EcoMap?> = _currentMap.asStateFlow()

    private val _selectedTab = MutableStateFlow(DetailTab.CANVAS)
    val selectedTab: StateFlow<DetailTab> = _selectedTab.asStateFlow()

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile.asStateFlow()

    private val _pdfUri = MutableStateFlow<Uri?>(null)
    val pdfUri: StateFlow<Uri?> = _pdfUri.asStateFlow()

    fun loadEcoMap(id: String) {
        viewModelScope.launch {
            invalidatePdf()
            val map = repository.getEcoMapById(id)
            _currentMap.value = map
        }
    }

    private fun invalidatePdf() {
        _pdfFile.value = null
        _pdfUri.value = null
    }

    fun setSelectedTab(tab: DetailTab) {
        _selectedTab.value = tab
    }

    fun selectNode(nodeId: String?) {
        _selectedNodeId.value = nodeId
    }

    fun updateNodePosition(nodeId: String, newRelativeX: Float, newRelativeY: Float) {
        val map = _currentMap.value ?: return
        val updatedNodes = map.nodes.map { node ->
            if (node.id == nodeId) {
                node.copy(
                    relativeX = newRelativeX.coerceIn(0.08f, 0.92f),
                    relativeY = newRelativeY.coerceIn(0.10f, 0.90f)
                )
            } else node
        }
        val updatedMap = map.copy(nodes = updatedNodes)
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun addOrUpdateNode(node: EcoMapNode, connectionType: ConnectionType?, flowDirection: FlowDirection?) {
        val map = _currentMap.value ?: return
        val existingIndex = map.nodes.indexOfFirst { it.id == node.id }
        val updatedNodes = if (existingIndex >= 0) {
            map.nodes.toMutableList().apply { set(existingIndex, node) }
        } else {
            map.nodes + node
        }

        // Manage connection if type is provided
        val updatedConnections = map.connections.toMutableList()
        if (connectionType != null) {
            val connIndex = updatedConnections.indexOfFirst { it.targetNodeId == node.id }
            val conn = EcoMapConnection(
                id = if (connIndex >= 0) updatedConnections[connIndex].id else java.util.UUID.randomUUID().toString(),
                ecomapId = map.id,
                targetNodeId = node.id,
                connectionType = connectionType,
                flowDirection = flowDirection ?: connectionType.defaultFlow
            )
            if (connIndex >= 0) {
                updatedConnections[connIndex] = conn
            } else {
                updatedConnections.add(conn)
            }
        }

        val updatedMap = map.copy(nodes = updatedNodes, connections = updatedConnections)
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun deleteNode(nodeId: String) {
        val map = _currentMap.value ?: return
        val updatedNodes = map.nodes.filterNot { it.id == nodeId }
        val updatedConnections = map.connections.filterNot { it.targetNodeId == nodeId }
        val updatedMap = map.copy(nodes = updatedNodes, connections = updatedConnections)
        _currentMap.value = updatedMap
        if (_selectedNodeId.value == nodeId) _selectedNodeId.value = null
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun updateConnection(targetNodeId: String, type: ConnectionType, flow: FlowDirection, notes: String = "") {
        val map = _currentMap.value ?: return
        val connIndex = map.connections.indexOfFirst { it.targetNodeId == targetNodeId }
        val updatedConnections = map.connections.toMutableList()
        val newConn = EcoMapConnection(
            id = if (connIndex >= 0) updatedConnections[connIndex].id else java.util.UUID.randomUUID().toString(),
            ecomapId = map.id,
            targetNodeId = targetNodeId,
            connectionType = type,
            flowDirection = flow,
            notes = notes
        )
        if (connIndex >= 0) {
            updatedConnections[connIndex] = newConn
        } else {
            updatedConnections.add(newConn)
        }
        val updatedMap = map.copy(connections = updatedConnections)
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun updateClientDetails(
        clientName: String,
        title: String,
        caseId: String,
        therapistName: String,
        facilityName: String,
        supervisorName: String,
        householdMembers: List<HouseholdMember>
    ) {
        val map = _currentMap.value ?: return
        val updatedMap = map.copy(
            clientName = clientName,
            title = title,
            caseId = caseId,
            therapistName = therapistName,
            facilityName = facilityName,
            supervisorName = supervisorName,
            householdMembers = householdMembers
        )
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun updateClinicalNotes(
        presentingContext: String,
        clinicalImpressions: String,
        recommendedInterventions: String = ""
    ) {
        val map = _currentMap.value ?: return
        val updatedMap = map.copy(
            presentingContext = presentingContext,
            clinicalImpressions = clinicalImpressions,
            recommendedInterventions = recommendedInterventions
        )
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun applyCircularLayout() {
        val map = _currentMap.value ?: return
        if (map.nodes.isEmpty()) return
        val total = map.nodes.size
        val radiusX = 0.35f
        val radiusY = 0.33f
        val centerX = 0.5f
        val centerY = 0.5f

        val updatedNodes = map.nodes.mapIndexed { index, node ->
            val angle = (2 * Math.PI * index / total) - (Math.PI / 2) // Start top
            val nx = (centerX + radiusX * cos(angle)).toFloat()
            val ny = (centerY + radiusY * sin(angle)).toFloat()
            node.copy(relativeX = nx, relativeY = ny)
        }
        val updatedMap = map.copy(nodes = updatedNodes)
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun applySymmetricCategoryLayout() {
        val map = _currentMap.value ?: return
        if (map.nodes.isEmpty()) return

        // Distribute left vs right columns based on index
        val updatedNodes = map.nodes.mapIndexed { index, node ->
            val isLeft = index % 2 == 0
            val row = index / 2
            val totalRows = (map.nodes.size + 1) / 2
            val yPos = 0.20f + (row * (0.60f / maxOf(1, totalRows - 1)))
            val xPos = if (isLeft) 0.18f else 0.82f
            node.copy(relativeX = xPos, relativeY = yPos)
        }
        val updatedMap = map.copy(nodes = updatedNodes)
        _currentMap.value = updatedMap
        invalidatePdf()
        saveCurrentState(updatedMap)
    }

    fun generatePdfReport(context: Context) {
        val map = _currentMap.value ?: return
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            withContext(Dispatchers.IO) {
                try {
                    val file = PdfReportGenerator.generateReport(context, map)
                    val uri = PdfReportGenerator.getUriForFile(context, file)
                    _pdfFile.value = file
                    _pdfUri.value = uri
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isGeneratingPdf.value = false
                }
            }
        }
    }

    private fun saveCurrentState(map: EcoMap) {
        viewModelScope.launch {
            repository.saveEcoMap(map)
        }
    }
}
