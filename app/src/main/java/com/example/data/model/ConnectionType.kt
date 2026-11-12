package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class FlowDirection(val displayName: String) {
    TOWARDS_CLIENT("Flows to Client (Giving/Support)"),
    AWAY_FROM_CLIENT("Flows to Domain (Draining/Effort)"),
    BIDIRECTIONAL("Reciprocal Flow (Mutual Exchange)"),
    NONE("Neutral / Unspecified")
}

enum class ConnectionType(
    val displayName: String,
    val description: String,
    val color: Color,
    val defaultFlow: FlowDirection
) {
    STRONG(
        displayName = "Strong / Nurturing",
        description = "Solid, supportive relationship providing security and positive attachment.",
        color = ColorStrongNurturing,
        defaultFlow = FlowDirection.TOWARDS_CLIENT
    ),
    STRESSFUL(
        displayName = "Stressful / Conflictual",
        description = "High conflict, anxiety, friction, or environmental stress.",
        color = ColorStressfulConflict,
        defaultFlow = FlowDirection.AWAY_FROM_CLIENT
    ),
    TENUOUS(
        displayName = "Tenuous / Weak",
        description = "Fragile, unreliable, distant, or uncertain connection.",
        color = ColorTenuousWeak,
        defaultFlow = FlowDirection.NONE
    ),
    DRAINING(
        displayName = "Energy Draining",
        description = "Heavy emotional/financial drain depleting client resources.",
        color = ColorEnergyDraining,
        defaultFlow = FlowDirection.AWAY_FROM_CLIENT
    ),
    GIVING(
        displayName = "Energy Giving / Resource",
        description = "Active resource input vital for resilience and well-being.",
        color = ColorEnergyGiving,
        defaultFlow = FlowDirection.TOWARDS_CLIENT
    ),
    RECIPROCAL(
        displayName = "Reciprocal Support",
        description = "Mutual balanced exchange of energy and support.",
        color = ColorReciprocal,
        defaultFlow = FlowDirection.BIDIRECTIONAL
    );

    companion object {
        fun fromString(value: String): ConnectionType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: STRONG
        }
    }
}
