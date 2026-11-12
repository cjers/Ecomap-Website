package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EcoMap
import com.example.data.model.HouseholdMember

@Composable
fun ClinicalNotesView(
    ecomap: EcoMap,
    onUpdateClientDetails: (String, String, String, String, String, String, List<HouseholdMember>) -> Unit,
    onUpdateClinicalNotes: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var clientName by remember(ecomap) { mutableStateOf(ecomap.clientName) }
    var title by remember(ecomap) { mutableStateOf(ecomap.title) }
    var caseId by remember(ecomap) { mutableStateOf(ecomap.caseId) }
    var therapistName by remember(ecomap) { mutableStateOf(ecomap.therapistName) }
    var facilityName by remember(ecomap) { mutableStateOf(ecomap.facilityName) }
    var supervisorName by remember(ecomap) { mutableStateOf(ecomap.supervisorName) }

    var presentingContext by remember(ecomap) { mutableStateOf(ecomap.presentingContext) }
    var clinicalImpressions by remember(ecomap) { mutableStateOf(ecomap.clinicalImpressions) }
    var recommendedInterventions by remember(ecomap) { mutableStateOf(ecomap.recommendedInterventions) }

    val membersList = remember(ecomap) { mutableStateListOf(*ecomap.householdMembers.toTypedArray()) }

    var showAddMemberDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("clinical_notes_view")
    ) {
        // Section 1: Client Metadata & Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Client & Facility Metadata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it; onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList) },
                        label = { Text("Client Name / Initials") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                            .testTag("client_name_input")
                    )
                    OutlinedTextField(
                        value = caseId,
                        onValueChange = { caseId = it; onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList) },
                        label = { Text("Case ID") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                            .testTag("case_id_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList) },
                    label = { Text("Assessment Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assessment_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = facilityName,
                    onValueChange = { facilityName = it; onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList) },
                    label = { Text("Facility / Clinic Name (Appears on PDF Header)") },
                    placeholder = { Text("e.g. Beacon Behavioral Health Center") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("facility_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = therapistName,
                        onValueChange = { therapistName = it; onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList) },
                        label = { Text("Therapist Name") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )
                    OutlinedTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it; onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList) },
                        label = { Text("Clinical Supervisor") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Household Members
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Household System Composition (${membersList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { showAddMemberDialog = true },
                        modifier = Modifier.testTag("add_household_member_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Member")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                membersList.forEachIndexed { index, member ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${member.relation}${if (member.age.isNotBlank()) ", Age ${member.age}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    membersList.removeAt(index)
                                    onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList)
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Clinical Narrative Fields
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Clinical Assessment Narrative",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = presentingContext,
                    onValueChange = {
                        presentingContext = it
                        onUpdateClinicalNotes(presentingContext, clinicalImpressions)
                    },
                    label = { Text("Presenting Problem & Ecological Context") },
                    placeholder = { Text("Describe client's reason for referral, environmental stress, housing/work context...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("presenting_context_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = clinicalImpressions,
                    onValueChange = {
                        clinicalImpressions = it
                        onUpdateClinicalNotes(presentingContext, clinicalImpressions)
                    },
                    label = { Text("Therapist Systemic Impressions & Resilience Assets") },
                    placeholder = { Text("Detail protective factors, informal support network resilience, conflict points...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clinical_impressions_input")
                )
            }
        }
    }

    // Add Household Member Dialog
    if (showAddMemberDialog) {
        var mName by remember { mutableStateOf("") }
        var mRelation by remember { mutableStateOf("Family Member") }
        var mAge by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Household Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = mName,
                        onValueChange = { mName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mRelation,
                        onValueChange = { mRelation = it },
                        label = { Text("Relation (e.g. Son, Partner)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mAge,
                        onValueChange = { mAge = it },
                        label = { Text("Age (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mName.isNotBlank()) {
                            membersList.add(HouseholdMember(mName, mRelation, mAge))
                            onUpdateClientDetails(clientName, title, caseId, therapistName, facilityName, supervisorName, membersList)
                            showAddMemberDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
