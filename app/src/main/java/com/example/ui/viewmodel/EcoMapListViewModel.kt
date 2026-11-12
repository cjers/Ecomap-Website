package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EcoMap
import com.example.data.model.HouseholdMember
import com.example.data.repository.EcoMapRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EcoMapListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EcoMapRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val ecoMaps: StateFlow<List<EcoMap>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getEcoMaps()
            } else {
                repository.searchEcoMaps(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteEcoMap(id: String) {
        viewModelScope.launch {
            repository.deleteEcoMap(id)
        }
    }

    fun createNewEcoMap(clientName: String, title: String, caseId: String): String {
        val newId = "map-${UUID.randomUUID().toString().take(8)}"
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val newMap = EcoMap(
            id = newId,
            title = title.ifBlank { "Client Systemic Assessment" },
            clientName = clientName.ifBlank { "New Client" },
            caseId = caseId.ifBlank { "EC-${(1000..9999).random()}" },
            dateCreated = now,
            dateUpdated = now,
            householdMembers = listOf(HouseholdMember(clientName.ifBlank { "New Client" }, "Self"))
        )
        viewModelScope.launch {
            repository.saveEcoMap(newMap)
        }
        return newId
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }
}
