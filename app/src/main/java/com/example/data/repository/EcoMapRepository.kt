package com.example.data.repository

import android.content.Context
import com.example.data.db.*
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EcoMapRepository(context: Context) {
    private val db = EcoMapDatabase.getDatabase(context)
    private val dao = db.ecomapDao()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val membersType = Types.newParameterizedType(List::class.java, HouseholdMember::class.java)
    private val membersAdapter = moshi.adapter<List<HouseholdMember>>(membersType)

    fun getEcoMaps(): Flow<List<EcoMap>> {
        return dao.getAllEcoMapsWithDetails().map { list ->
            list.map { toDomainEcoMap(it) }
        }
    }

    fun searchEcoMaps(query: String): Flow<List<EcoMap>> {
        return dao.searchEcoMaps(query).map { list ->
            list.map { toDomainEcoMap(it) }
        }
    }

    suspend fun getEcoMapById(id: String): EcoMap? {
        val entity = dao.getEcoMapWithDetailsById(id) ?: return null
        return toDomainEcoMap(entity)
    }

    suspend fun saveEcoMap(map: EcoMap) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val updatedMap = if (map.dateCreated.isEmpty()) map.copy(dateCreated = now, dateUpdated = now) else map.copy(dateUpdated = now)

        val mapEntity = EcoMapEntity(
            id = updatedMap.id,
            title = updatedMap.title,
            clientName = updatedMap.clientName,
            caseId = updatedMap.caseId,
            dateCreated = updatedMap.dateCreated,
            dateUpdated = updatedMap.dateUpdated,
            therapistName = updatedMap.therapistName,
            facilityName = updatedMap.facilityName,
            supervisorName = updatedMap.supervisorName,
            presentingContext = updatedMap.presentingContext,
            clinicalImpressions = updatedMap.clinicalImpressions,
            recommendedInterventions = updatedMap.recommendedInterventions,
            householdMembersJson = membersAdapter.toJson(updatedMap.householdMembers)
        )

        val nodeEntities = updatedMap.nodes.map { node ->
            NodeEntity(
                id = node.id,
                ecomapId = updatedMap.id,
                title = node.title,
                category = node.category.name,
                impactRating = node.impactRating,
                notes = node.notes,
                relativeX = node.relativeX,
                relativeY = node.relativeY
            )
        }

        val connectionEntities = updatedMap.connections.map { conn ->
            ConnectionEntity(
                id = conn.id,
                ecomapId = updatedMap.id,
                targetNodeId = conn.targetNodeId,
                connectionType = conn.connectionType.name,
                flowDirection = conn.flowDirection.name,
                notes = conn.notes
            )
        }

        dao.saveFullEcoMap(mapEntity, nodeEntities, connectionEntities)
    }

    suspend fun deleteEcoMap(id: String) {
        dao.deleteEcoMapById(id)
    }

    suspend fun seedSampleDataIfEmpty() {
        // Seed if DB is empty
        dao.getAllEcoMapsWithDetails().map { list ->
            if (list.isEmpty()) {
                seedCases()
            }
        }
    }

    suspend fun seedInitialData() {
        seedCases()
    }

    private suspend fun seedCases() {
        val case1Id = "case-sarah-jenkins"
        val node1 = EcoMapNode(id = "n1", ecomapId = case1Id, title = "St. Jude Elementary (3rd Grade)", category = DomainCategory.SCHOOL, impactRating = 4, notes = "School counselor offers IEP support for son Leo.", relativeX = 0.50f, relativeY = 0.15f)
        val node2 = EcoMapNode(id = "n2", ecomapId = case1Id, title = "County Hospital (Night Shift)", category = DomainCategory.WORK, impactRating = 5, notes = "Mandatory 50 hr/wk shift work causing severe physical fatigue.", relativeX = 0.82f, relativeY = 0.30f)
        val node3 = EcoMapNode(id = "n3", ecomapId = case1Id, title = "Grandma Martha (Maternal)", category = DomainCategory.FAMILY, impactRating = 4, notes = "Provides vital emergency childcare twice weekly.", relativeX = 0.85f, relativeY = 0.65f)
        val node4 = EcoMapNode(id = "n4", ecomapId = case1Id, title = "City Housing Authority", category = DomainCategory.LEGAL, impactRating = 5, notes = "Section 8 voucher review pending. High eviction anxiety.", relativeX = 0.50f, relativeY = 0.85f)
        val node5 = EcoMapNode(id = "n5", ecomapId = case1Id, title = "Elena (Best Friend)", category = DomainCategory.FRIENDS, impactRating = 3, notes = "Daily emotional listening & encouragement.", relativeX = 0.18f, relativeY = 0.68f)
        val node6 = EcoMapNode(id = "n6", ecomapId = case1Id, title = "Grace Community Pantry", category = DomainCategory.COMMUNITY, impactRating = 3, notes = "Weekly food supplements and single-mom support group.", relativeX = 0.15f, relativeY = 0.32f)

        val map1 = EcoMap(
            id = case1Id,
            title = "Single Parent Systemic Assessment",
            clientName = "Sample",
            caseId = "EC-2026-8812",
            dateCreated = "2026-11-01 09:30",
            dateUpdated = "2026-11-06 14:15",
            therapistName = "Dr. Alex Taylor, LMFT",
            facilityName = "Stonetree Therapy",
            supervisorName = "CJ Webster LMFT",
            presentingContext = "Sarah (34) presents with chronic anxiety, sleep disturbance, and overwhelming parental stress regarding housing stability and long shift work hours.",
            clinicalImpressions = "Client demonstrates remarkable internal resilience and leverages informal support assets (Friend Elena, Grandma Martha). Primary systemic stressors stem from institutional housing bureaucracy and employment overwork.",
            recommendedInterventions = "1. Emergency advocacy letter for Housing Authority voucher retention.\n2. Sleep hygiene & stress reduction pacing.\n3. Explore local subsidies to reduce mandatory hospital overtime.",
            householdMembers = listOf(
                HouseholdMember("Sample", "Self / Mother", "34"),
                HouseholdMember("Leo Jenkins", "Son", "8")
            ),
            nodes = listOf(node1, node2, node3, node4, node5, node6),
            connections = listOf(
                EcoMapConnection(id = "c1", ecomapId = case1Id, targetNodeId = "n1", connectionType = ConnectionType.STRONG, flowDirection = FlowDirection.TOWARDS_CLIENT, notes = "Active guidance for Leo"),
                EcoMapConnection(id = "c2", ecomapId = case1Id, targetNodeId = "n2", connectionType = ConnectionType.STRESSFUL, flowDirection = FlowDirection.AWAY_FROM_CLIENT, notes = "Severe energy drain & fatigue"),
                EcoMapConnection(id = "c3", ecomapId = case1Id, targetNodeId = "n3", connectionType = ConnectionType.GIVING, flowDirection = FlowDirection.TOWARDS_CLIENT, notes = "Childcare relief & emotional support"),
                EcoMapConnection(id = "c4", ecomapId = case1Id, targetNodeId = "n4", connectionType = ConnectionType.STRESSFUL, flowDirection = FlowDirection.BIDIRECTIONAL, notes = "Bureaucratic deadline friction"),
                EcoMapConnection(id = "c5", ecomapId = case1Id, targetNodeId = "n5", connectionType = ConnectionType.STRONG, flowDirection = FlowDirection.BIDIRECTIONAL, notes = "Reciprocal trust and respite"),
                EcoMapConnection(id = "c6", ecomapId = case1Id, targetNodeId = "n6", connectionType = ConnectionType.GIVING, flowDirection = FlowDirection.TOWARDS_CLIENT, notes = "Nurturing community resource")
            )
        )

        val case2Id = "case-arthur-miller"
        val m2n1 = EcoMapNode(id = "m2_n1", ecomapId = case2Id, title = "Senior Wellness Center", category = DomainCategory.COMMUNITY, impactRating = 4, notes = "Daily socialization and hot meals.", relativeX = 0.50f, relativeY = 0.18f)
        val m2n2 = EcoMapNode(id = "m2_n2", ecomapId = case2Id, title = "Primary Physician (Dr. Chen)", category = DomainCategory.HEALTHCARE, impactRating = 4, notes = "Regular health monitoring.", relativeX = 0.82f, relativeY = 0.40f)
        val m2n3 = EcoMapNode(id = "m2_n3", ecomapId = case2Id, title = "Adult Children (Out of State)", category = DomainCategory.FAMILY, impactRating = 3, notes = "Infrequent phone contact, distance gap.", relativeX = 0.70f, relativeY = 0.80f)
        val m2n4 = EcoMapNode(id = "m2_n4", ecomapId = case2Id, title = "Prescription & Pension Costs", category = DomainCategory.FINANCIAL, impactRating = 5, notes = "Strained fixed income.", relativeX = 0.25f, relativeY = 0.75f)

        val map2 = EcoMap(
            id = case2Id,
            title = "Geriatric Caregiver Systemic Map",
            clientName = "Arthur Miller",
            caseId = "EC-2026-4409",
            dateCreated = "2026-10-25 11:00",
            dateUpdated = "2026-11-04 16:30",
            therapistName = "Dr. Alex Taylor, LMFT",
            facilityName = "Stonetree Therapy",
            supervisorName = "CJ Webster LMFT",
            presentingContext = "Arthur (79) serves as primary caregiver for spouse Evelyn (77) with progressive memory loss. Experiencing caregiver burnout.",
            clinicalImpressions = "High caregiver burden with limited immediate family assistance. Senior Center provides vital respite.",
            recommendedInterventions = "1. Referral for home respite aide 10 hrs/week.\n2. Caregiver support group enrollment.",
            householdMembers = listOf(
                HouseholdMember("Arthur Miller", "Self", "79"),
                HouseholdMember("Evelyn Miller", "Spouse", "77")
            ),
            nodes = listOf(m2n1, m2n2, m2n3, m2n4),
            connections = listOf(
                EcoMapConnection(id = "m2_c1", ecomapId = case2Id, targetNodeId = "m2_n1", connectionType = ConnectionType.GIVING, flowDirection = FlowDirection.TOWARDS_CLIENT),
                EcoMapConnection(id = "m2_c2", ecomapId = case2Id, targetNodeId = "m2_n2", connectionType = ConnectionType.STRONG, flowDirection = FlowDirection.BIDIRECTIONAL),
                EcoMapConnection(id = "m2_c3", ecomapId = case2Id, targetNodeId = "m2_n3", connectionType = ConnectionType.TENUOUS, flowDirection = FlowDirection.NONE),
                EcoMapConnection(id = "m2_c4", ecomapId = case2Id, targetNodeId = "m2_n4", connectionType = ConnectionType.DRAINING, flowDirection = FlowDirection.AWAY_FROM_CLIENT)
            )
        )

        saveEcoMap(map1)
        saveEcoMap(map2)
    }

    private fun toDomainEcoMap(entity: EcoMapWithDetails): EcoMap {
        val map = entity.map
        val members = membersAdapter.fromJson(map.householdMembersJson) ?: emptyList()

        val nodes = entity.nodes.map { n ->
            EcoMapNode(
                id = n.id,
                ecomapId = n.ecomapId,
                title = n.title,
                category = DomainCategory.fromString(n.category),
                impactRating = n.impactRating,
                notes = n.notes,
                relativeX = n.relativeX,
                relativeY = n.relativeY
            )
        }

        val connections = entity.connections.map { c ->
            EcoMapConnection(
                id = c.id,
                ecomapId = c.ecomapId,
                targetNodeId = c.targetNodeId,
                connectionType = ConnectionType.fromString(c.connectionType),
                flowDirection = try { FlowDirection.valueOf(c.flowDirection) } catch (e: Exception) { FlowDirection.NONE },
                notes = c.notes
            )
        }

        return EcoMap(
            id = map.id,
            title = map.title,
            clientName = map.clientName,
            caseId = map.caseId,
            dateCreated = map.dateCreated,
            dateUpdated = map.dateUpdated,
            therapistName = map.therapistName,
            facilityName = map.facilityName,
            supervisorName = map.supervisorName,
            presentingContext = map.presentingContext,
            clinicalImpressions = map.clinicalImpressions,
            recommendedInterventions = map.recommendedInterventions,
            householdMembers = members,
            nodes = nodes,
            connections = connections
        )
    }
}
