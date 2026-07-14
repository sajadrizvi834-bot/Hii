package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val blockedItemDao: BlockedItemDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allBlockedItems: Flow<List<BlockedItem>> = blockedItemDao.getAllBlockedItems()
    val totalSpent: Flow<Double?> = expenseDao.getTotalSpent()

    suspend fun checkAndSeedDatabaseIfNeeded() {
        val expenses = expenseDao.getAllExpenses().first()
        val blockedItems = blockedItemDao.getAllBlockedItems().first()

        if (expenses.isEmpty() && blockedItems.isEmpty()) {
            // Seed initial social media and subscription block configurations
            blockedItemDao.insertBlockedItem(
                BlockedItem(
                    name = "Instagram Ads",
                    isBlocked = true,
                    category = "Social Media Ads",
                    redirectSuggestion = "Instagram ads are tailored to exploit your impulse cues. Take 24 hours to think before buying!"
                )
            )
            blockedItemDao.insertBlockedItem(
                BlockedItem(
                    name = "TikTok Shop",
                    isBlocked = true,
                    category = "Social Media Ads",
                    redirectSuggestion = "TikTok viral trends pass in a week, but the charge remains. Put this money in your savings vault!"
                )
            )
            blockedItemDao.insertBlockedItem(
                BlockedItem(
                    name = "Netflix Premium",
                    isBlocked = false,
                    category = "Digital Subscriptions",
                    redirectSuggestion = "Do you have unused screen tiers? Downgrading to standard or pausing could save you $120/year!"
                )
            )
            blockedItemDao.insertBlockedItem(
                BlockedItem(
                    name = "Premium Ad-Free Twitter/X",
                    isBlocked = true,
                    category = "Digital Subscriptions",
                    redirectSuggestion = "Social media premium subscriptions are low-yield. Use standard free tiers to save money."
                )
            )
            blockedItemDao.insertBlockedItem(
                BlockedItem(
                    name = "Spotify Premium",
                    isBlocked = false,
                    category = "Digital Subscriptions",
                    redirectSuggestion = "Spotify keeps the music going, but consider standard student or family tiers to split costs."
                )
            )

            // Seed initial sample expenses for beautiful analytics on first launch
            val now = System.currentTimeMillis()
            expenseDao.insertExpense(Expense(title = "Healthy Grocery Haul", amount = 64.50, category = "Food & Dining", timestamp = now - 86400000 * 2))
            expenseDao.insertExpense(Expense(title = "Monthly Bus Pass", amount = 45.00, category = "Transport", timestamp = now - 86400000 * 4))
            expenseDao.insertExpense(Expense(title = "Water & Gas Bill", amount = 85.00, category = "Utilities", timestamp = now - 86400000 * 5))
            expenseDao.insertExpense(Expense(title = "Instagram Viral Mug", amount = 22.00, category = "Social Media Ads", timestamp = now - 86400000 * 1, notes = "Purchased from IG Ad flow"))
            expenseDao.insertExpense(Expense(title = "Netflix Subscription", amount = 15.49, category = "Digital Subscriptions", timestamp = now - 86400000 * 3))
        }
    }

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Int) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun insertBlockedItem(item: BlockedItem) {
        blockedItemDao.insertBlockedItem(item)
    }

    suspend fun updateBlockedItem(item: BlockedItem) {
        blockedItemDao.updateBlockedItem(item)
    }

    suspend fun deleteBlockedItem(item: BlockedItem) {
        blockedItemDao.deleteBlockedItem(item)
    }

    suspend fun getBlockedItemByName(name: String): BlockedItem? {
        return blockedItemDao.getBlockedItemByName(name)
    }
}
