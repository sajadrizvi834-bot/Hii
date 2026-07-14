package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ExpenseDatabase
import com.example.data.ExpenseRepository
import com.example.ui.ExpenseViewModel
import com.example.ui.ExpenseViewModelFactory
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ExpenseTrackerScreen
import com.example.ui.screens.SuggestedFeedScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate database & repository
        val database = ExpenseDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = ExpenseRepository(database.expenseDao(), database.blockedItemDao())

        setContent {
            MyApplicationTheme {
                val expenseViewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory(application, repository)
                )

                var currentTab by remember { mutableStateOf("expenses") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "expenses",
                                onClick = { currentTab = "expenses" },
                                label = { Text("Expenses") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == "expenses") Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                        contentDescription = "Tracker"
                                    )
                                },
                                modifier = Modifier.testTag("nav_tracker")
                            )

                            NavigationBarItem(
                                selected = currentTab == "analytics",
                                onClick = { currentTab = "analytics" },
                                label = { Text("Analytics") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == "analytics") Icons.Filled.PieChart else Icons.Outlined.PieChart,
                                        contentDescription = "Analytics"
                                    )
                                },
                                modifier = Modifier.testTag("nav_analytics")
                            )

                            NavigationBarItem(
                                selected = currentTab == "feed",
                                onClick = { currentTab = "feed" },
                                label = { Text("Coach Feed") },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == "feed") Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                        contentDescription = "Suggested Feed"
                                    )
                                },
                                modifier = Modifier.testTag("nav_feed")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            "expenses" -> ExpenseTrackerScreen(
                                viewModel = expenseViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            "analytics" -> AnalyticsScreen(
                                viewModel = expenseViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            "feed" -> SuggestedFeedScreen(
                                viewModel = expenseViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
