package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food & Dining" -> Color(0xFF10B981)          // Emerald Green
        "Entertainment" -> Color(0xFFF59E0B)          // Amber
        "Transport" -> Color(0xFF3B82F6)              // Cool Blue
        "Social Media Ads" -> Color(0xFFEF4444)       // Warning/Coral Red
        "Digital Subscriptions" -> Color(0xFF8B5CF6)  // Royal Violet
        "Shopping" -> Color(0xFFEC4899)               // Rose Pink
        "Utilities" -> Color(0xFF06B6D4)              // Cyan
        "Other" -> Color(0xFF64748B)                  // Slate Gray
        else -> Color(0xFF6750A4)
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food & Dining" -> Icons.Default.Restaurant
        "Entertainment" -> Icons.Default.SportsEsports
        "Transport" -> Icons.Default.DirectionsCar
        "Social Media Ads" -> Icons.Default.Campaign
        "Digital Subscriptions" -> Icons.Default.Subscriptions
        "Shopping" -> Icons.Default.ShoppingBag
        "Utilities" -> Icons.Default.Lightbulb
        "Other" -> Icons.Default.LocalOffer
        else -> Icons.Default.ReceiptLong
    }
}
