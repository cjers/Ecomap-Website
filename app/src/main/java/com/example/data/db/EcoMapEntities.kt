package com.example.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.example.data.model.DomainCategory
import com.example.data.model.ConnectionType
import com.example.data.model.FlowDirection
import com.example.data.model.HouseholdMember
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "ecomaps")
data class EcoMapEntity(
    @PrimaryKey val id: String,
    val title: String,
    val clientName: String,
    val caseId: String,
    val dateCreated: String,
    val dateUpdated: String,
    val therapistName: String,
    val facilityName: String,
    val supervisorName: String,
    val presentingContext: String,
    val clinicalImpressions: String,
    val recommendedInterventions: String,
    val householdMembersJson: String
)

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey val id: String,
    val ecomapId: String,
    val title: String,
    val category: String,
    val impactRating: Int,
    val notes: String,
    val relativeX: Float,
    val relativeY: Float
)

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val id: String,
    val ecomapId: String,
    val targetNodeId: String,
    val connectionType: String,
    val flowDirection: String,
    val notes: String
)

data class EcoMapWithDetails(
    @Embedded val map: EcoMapEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ecomapId"
    )
    val nodes: List<NodeEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "ecomapId"
    )
    val connections: List<ConnectionEntity>
)

class DatabaseConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val membersType = Types.newParameterizedType(List::class.java, HouseholdMember::class.java)
    private val membersAdapter = moshi.adapter<List<HouseholdMember>>(membersType)

    @TypeConverter
    fun fromHouseholdMembersList(members: List<HouseholdMember>?): String {
        return membersAdapter.toJson(members ?: emptyList())
    }

    @TypeConverter
    fun toHouseholdMembersList(json: String?): List<HouseholdMember> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            membersAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
