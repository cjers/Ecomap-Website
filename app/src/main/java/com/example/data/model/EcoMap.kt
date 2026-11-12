package com.example.data.model

import java.util.UUID

data class HouseholdMember(
    val name: String,
    val relation: String,
    val age: String = ""
)

data class EcoMapNode(
    val id: String = UUID.randomUUID().toString(),
    val ecomapId: String,
    val title: String,
    val category: DomainCategory,
    val impactRating: Int = 3, // 1 (Minor) to 5 (Severe / Vital)
    val notes: String = "",
    val relativeX: Float = 0.5f, // Normalized 0.0 .. 1.0 canvas coordinate
    val relativeY: Float = 0.5f
)

data class EcoMapConnection(
    val id: String = UUID.randomUUID().toString(),
    val ecomapId: String,
    val targetNodeId: String, // ID of the surrounding domain node
    val connectionType: ConnectionType = ConnectionType.STRONG,
    val flowDirection: FlowDirection = ConnectionType.STRONG.defaultFlow,
    val notes: String = ""
)

data class EcoMap(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val clientName: String,
    val caseId: String,
    val dateCreated: String,
    val dateUpdated: String,
    val therapistName: String = "Dr. Alex Taylor, LCSW",
    val facilityName: String = "Beacon Behavioral Health Center",
    val supervisorName: String = "Dr. Morgan Reed, PsyD",
    val presentingContext: String = "",
    val clinicalImpressions: String = "",
    val recommendedInterventions: String = "",
    val householdMembers: List<HouseholdMember> = emptyList(),
    val nodes: List<EcoMapNode> = emptyList(),
    val connections: List<EcoMapConnection> = emptyList()
) {
    // Analytics calculations
    val totalDomains: Int get() = nodes.size
    val nurturingCount: Int get() = connections.count { it.connectionType == ConnectionType.STRONG || it.connectionType == ConnectionType.GIVING }
    val stressfulCount: Int get() = connections.count { it.connectionType == ConnectionType.STRESSFUL || it.connectionType == ConnectionType.DRAINING }
    val tenuousCount: Int get() = connections.count { it.connectionType == ConnectionType.TENUOUS }
}
