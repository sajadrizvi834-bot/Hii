package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
)

@Entity(tableName = "blocked_items")
data class BlockedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isBlocked: Boolean,
    val category: String, // e.g., "Social Media Impulse", "Digital Subscriptions"
    val limitAmount: Double = 0.0,
    val redirectSuggestion: String
)
