package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EcoMapDao {
    @Transaction
    @Query("SELECT * FROM ecomaps ORDER BY dateUpdated DESC")
    fun getAllEcoMapsWithDetails(): Flow<List<EcoMapWithDetails>>

    @Transaction
    @Query("SELECT * FROM ecomaps WHERE id = :id")
    suspend fun getEcoMapWithDetailsById(id: String): EcoMapWithDetails?

    @Transaction
    @Query("SELECT * FROM ecomaps WHERE clientName LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR caseId LIKE '%' || :query || '%'")
    fun searchEcoMaps(query: String): Flow<List<EcoMapWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEcoMap(map: EcoMapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<NodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<ConnectionEntity>)

    @Query("DELETE FROM nodes WHERE ecomapId = :ecomapId")
    suspend fun deleteNodesForEcoMap(ecomapId: String)

    @Query("DELETE FROM connections WHERE ecomapId = :ecomapId")
    suspend fun deleteConnectionsForEcoMap(ecomapId: String)

    @Query("DELETE FROM ecomaps WHERE id = :id")
    suspend fun deleteEcoMapById(id: String)

    @Transaction
    suspend fun saveFullEcoMap(
        mapEntity: EcoMapEntity,
        nodeEntities: List<NodeEntity>,
        connectionEntities: List<ConnectionEntity>
    ) {
        insertEcoMap(mapEntity)
        deleteNodesForEcoMap(mapEntity.id)
        deleteConnectionsForEcoMap(mapEntity.id)
        if (nodeEntities.isNotEmpty()) insertNodes(nodeEntities)
        if (connectionEntities.isNotEmpty()) insertConnections(connectionEntities)
    }
}
