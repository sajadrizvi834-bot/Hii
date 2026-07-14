package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.*

// Data class representation for bento category aggregations
data class CategorySummaryItem(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()

    var activeChartType by remember { mutableStateOf("donut") } // "donut" or "bar"
    var selectedCategoryForTooltip by remember { mutableStateOf<String?>(null) }

    val isDark = isSystemInDarkTheme()

    // Group expenses by category
    val categorySummary = remember(expenses, totalSpent) {
        expenses.groupBy { it.category }
            .map { entry ->
                val amountSum = entry.value.sumOf { it.amount }
                CategorySummaryItem(
                    category = entry.key,
                    amount = amountSum,
                    percentage = if (totalSpent > 0) (amountSum / totalSpent).toFloat() else 0f,
                    count = entry.value.size
                )
            }.sortedByDescending { it.amount }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Analytics Empty",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analytics Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add expenses on the Tracker tab to visualize spending trends by category, check subscription ratios, and verify impulse buy patterns.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Headline Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Deconstruct your cash flows",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        // Chart Type Switcher - Bento Style rounded Segmented Row
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.height(40.dp)
                        ) {
                            SegmentedButton(
                                selected = activeChartType == "donut",
                                onClick = { activeChartType = "donut"; selectedCategoryForTooltip = null },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Icon(Icons.Default.PieChart, contentDescription = "Pie Chart", modifier = Modifier.size(16.dp))
                            }
                            SegmentedButton(
                                selected = activeChartType == "bar",
                                onClick = { activeChartType = "bar"; selectedCategoryForTooltip = null },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = "Bar Chart", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Interactive Chart Container Card (Bento Rounded)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) BentoSurfaceDark else BentoSurfaceLight
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (activeChartType == "donut") "Category Allocation" else "Top Expenditures",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (activeChartType == "donut") {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    InteractiveDonutChart(
                                        categorySummary = categorySummary,
                                        onCategoryHover = { selectedCategoryForTooltip = it }
                                    )

                                    // Centered tooltip text inside bento donut
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val displayCategory = selectedCategoryForTooltip ?: categorySummary.firstOrNull()?.category ?: ""
                                        val displayAmount = categorySummary.firstOrNull { it.category == displayCategory }?.amount ?: 0.0
                                        val displayPercent = categorySummary.firstOrNull { it.category == displayCategory }?.percentage ?: 0f

                                        Text(
                                            text = displayCategory,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", displayAmount)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = getCategoryColor(displayCategory)
                                        )
                                        Text(
                                            text = "${String.format("%.1f", displayPercent * 100)}% of total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            } else {
                                InteractiveBarChart(
                                    categorySummary = categorySummary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(horizontal = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "💡 Tap breakdown items below to deconstruct metrics",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                // Guard Insights Alert Card (Impulse metrics) - Bento Sage/Coral Style
                item {
                    val impulseSpent = categorySummary.firstOrNull { it.category == "Social Media Ads" }?.amount ?: 0.0
                    val subSpent = categorySummary.firstOrNull { it.category == "Digital Subscriptions" }?.amount ?: 0.0
                    val combinedDangerousSpent = impulseSpent + subSpent

                    if (combinedDangerousSpent > 0.0) {
                        val isHighLeakage = combinedDangerousSpent > totalSpent * 0.3
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighLeakage) {
                                    if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color(0xFFFEE2E2)
                                } else {
                                    if (isDark) Color(0xFF14532D).copy(alpha = 0.3f) else BentoSageGreen
                                }
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isHighLeakage) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isHighLeakage) {
                                                Color(0xFFEF4444).copy(alpha = 0.15f)
                                            } else {
                                                Color(0xFF10B981).copy(alpha = 0.15f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Analysis Alert",
                                        tint = if (isHighLeakage) Color(0xFFEF4444) else Color(0xFF10B981)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isHighLeakage) "⚠️ High Outflow Alert" else "🛡️ Guard System Green",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isHighLeakage) {
                                            if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                                        } else {
                                            if (isDark) Color(0xFF86EFAC) else BentoSageText
                                        }
                                    )
                                    Text(
                                        text = if (isHighLeakage) {
                                            "Impulse buy triggers and subscriptions account for ${String.format("%.1f", (combinedDangerousSpent / totalSpent) * 100)}% of tracked wealth outflow. Review your blockers to seal this leakage!"
                                        } else {
                                            "Superb self-discipline. Non-essential subscription drains are limited to ${String.format("%.1f", (combinedDangerousSpent / totalSpent) * 100)}% of your expenses."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Legend and details header
                item {
                    Text(
                        text = "Category Breakdowns",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Category Breakdowns list items
                items(categorySummary) { item ->
                    CategoryBreakdownRow(
                        item = item,
                        isSelected = selectedCategoryForTooltip == item.category,
                        onSelect = {
                            selectedCategoryForTooltip = if (selectedCategoryForTooltip == item.category) null else item.category
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveDonutChart(
    categorySummary: List<CategorySummaryItem>,
    onCategoryHover: (String) -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val chartSweepProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val sizeMin = minOf(width, height)
        val strokeWidthVal = sizeMin * 0.16f

        var startAngle = -90f

        categorySummary.forEach { item ->
            val sweepAngle = item.percentage * 360f
            
            drawArc(
                color = getCategoryColor(item.category),
                startAngle = startAngle,
                sweepAngle = sweepAngle * chartSweepProgress,
                useCenter = false,
                style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round),
                size = Size(sizeMin - strokeWidthVal, sizeMin - strokeWidthVal),
                topLeft = Offset((width - sizeMin + strokeWidthVal) / 2f, (height - sizeMin + strokeWidthVal) / 2f)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun InteractiveBarChart(
    categorySummary: List<CategorySummaryItem>,
    modifier: Modifier = Modifier
) {
    val maxVal = remember(categorySummary) { categorySummary.maxOfOrNull { it.amount } ?: 1.0 }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        categorySummary.take(5).forEach { item ->
            val barRatio = (item.amount / maxVal).toFloat()
            var animationPlayed by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = item) { animationPlayed = true }
            
            val animatedHeightRatio by animateFloatAsState(
                targetValue = if (animationPlayed) barRatio else 0f,
                animationSpec = tween(durationMillis = 800)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$${String.format("%.0f", item.amount)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(item.category)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Rounded Vertical Bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.7f * animatedHeightRatio)
                        .width(26.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(getCategoryColor(item.category))
                )
                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    imageVector = getCategoryIcon(item.category),
                    contentDescription = null,
                    tint = getCategoryColor(item.category).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.category.length > 5) item.category.take(4) + ".." else item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownRow(
    item: CategorySummaryItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                if (isDark) BentoSurfaceDark else BentoSurfaceLight
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored status indicator block
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(getCategoryColor(item.category))
                )

                Column {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.count} items tracked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", item.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = getCategoryColor(item.category)
                )
                Text(
                    text = "${String.format("%.1f", item.percentage * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
