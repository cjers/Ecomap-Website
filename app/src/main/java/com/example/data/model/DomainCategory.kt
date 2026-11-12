package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

enum class DomainCategory(
    val displayName: String,
    val defaultColor: Color,
    val icon: ImageVector
) {
    WORK("Work / Employment", CategoryWorkColor, Icons.Default.Work),
    SCHOOL("School / Education", CategorySchoolColor, Icons.AutoMirrored.Filled.MenuBook),
    HEALTHCARE("Medical & Therapy", CategoryHealthcareColor, Icons.Default.LocalHospital),
    HOUSING("Housing & Living", CategoryHousingColor, Icons.Default.Home),
    FRIENDS("Friends & Peers", CategoryFriendsColor, Icons.Default.People),
    FAMILY("Extended Family", CategoryFamilyColor, Icons.Default.FamilyRestroom),
    LEGAL("Legal & Social Services", CategoryLegalColor, Icons.Default.Gavel),
    COMMUNITY("Community & Faith", CategoryCommunityColor, Icons.Default.Church),
    FINANCIAL("Financial & Debt", CategoryFinancialColor, Icons.Default.AttachMoney),
    PETS("Pets & Caregiving", CategoryPetsColor, Icons.Default.Pets),
    OTHER("Custom Domain", CategoryOtherColor, Icons.Default.BubbleChart);

    companion object {
        fun fromString(value: String): DomainCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
