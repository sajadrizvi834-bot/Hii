package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Expense
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseTrackerScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val blockedItems by viewModel.blockedItems.collectAsState()
    val triggeredBlock by viewModel.triggeredBlock.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    // Determine top category
    val topCategory = remember(expenses) {
        if (expenses.isEmpty()) "Coffee Shops" else {
            expenses.groupBy { it.category }
                .maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "Other"
        }
    }
    val topCategoryAmount = remember(expenses) {
        if (expenses.isEmpty()) 124.00 else {
            expenses.filter { it.category == topCategory }.sumOf { it.amount }
        }
    }

    // Determine protection coverage score based on blocked items
    val protectionRate = remember(blockedItems) {
        val active = blockedItems.count { it.isBlocked }
        val total = blockedItems.size
        if (total == 0) 85f else ((active.toFloat() / total.toFloat()) * 100f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header Block (Bento-style Minimal Header)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Morning",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "SmartSpend Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Avatar bubble representing user "SR"
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) BentoPrimary.copy(alpha = 0.4f) else Color(0xFFD0E4FF))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SR",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1D4ED8)
                        )
                    }
                }
            }

            // Bento Grid Content Row 1: Large Lavender Spending Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) BentoSurfaceDark else BentoLavender
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) BentoBorderDark else BentoLavenderText.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Spending",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else BentoLavenderText
                                )
                                Text(
                                    text = "$${String.format("%.2f", totalSpent)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF1D1B20),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Dynamic Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Guarded Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF86EFAC) else Color(0xFF15803D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Dynamic Native Sparkline Graph representing expense history
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            BentoSparkline(expenses = expenses, isDark = isDark)
                        }
                    }
                }
            }

            // Bento Grid Row 2: Split columns for Top Spend and Guards Goal
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mini Bento Card 1: Top Spend Category
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) BentoSurfaceDark else BentoSurfaceLight
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isDark) BentoPrimary.copy(alpha = 0.2f) else Color(0xFFFFEDD5)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(topCategory),
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF86EFAC) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "TOP CATEGORY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = topCategory,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$${String.format("%.2f", topCategoryAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Mini Bento Card 2: Guards active safety rating (Sage Green Card)
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF14532D).copy(alpha = 0.4f) else BentoSageGreen
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF15803D).copy(alpha = 0.3f) else BentoSageText.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROTECTION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF86EFAC) else BentoSageText,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${protectionRate.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else BentoSageText
                                )
                            }

                            // Linear Progress Bar
                            LinearProgressIndicator(
                                progress = { protectionRate / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (isDark) Color(0xFF86EFAC) else Color(0xFF16A34A),
                                trackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
                            )

                            Text(
                                text = "Shield Barrier Active",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else BentoSageText,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Recent Transactions List (modular Bento Items)
            if (expenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = "Wallet Empty",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your cashbook is pristine",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Press '+' to add transactions & configure your subscription shields.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseItemRow(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(expense) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .animateItem()
                    )
                }
            }
        }

        // Floating Action Button - Bento Style (Rounded 2xl shape, translated offset)
        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = BentoPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("add_expense_fab"),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // Modal Dialog to Add Expense
    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAddExpense = { title, amount, category, notes ->
                viewModel.attemptAddExpense(title, amount, category, notes)
                showAddDialog = false
            }
        )
    }

    // Alert Dialog when Content Block triggers (styled beautiful bento overlay style)
    if (triggeredBlock != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearPendingExpense() },
            icon = {
                Icon(
                    imageVector = Icons.Default.GppBad,
                    contentDescription = "Content Block Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "✋ Content Block Triggered!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "You are attempting to purchase from blocked target: ${triggeredBlock?.name}.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "💡 Intelligent Guard Suggestion:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = triggeredBlock?.redirectSuggestion ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = "If this is absolute necessity, bypass below. Otherwise, let's keep your saving stream safe!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingExpense() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_block_bypass")
                ) {
                    Text("Bypass and Log")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.clearPendingExpense() },
                    modifier = Modifier.testTag("cancel_block_bypass")
                ) {
                    Text("Cancel Transaction", color = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val sdf = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val dateString = remember(expense.timestamp) { sdf.format(Date(expense.timestamp)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) BentoSurfaceDark else BentoSurfaceLight
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category circular block with dynamic color
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(getCategoryColor(expense.category).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = expense.category,
                        tint = getCategoryColor(expense.category),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$dateString • ${expense.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (!expense.notes.isNullOrBlank()) {
                        Text(
                            text = expense.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "-$${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = getCategoryColor(expense.category)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAddExpense: (String, Double, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food & Dining") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food & Dining", "Entertainment", "Transport", "Social Media Ads",
        "Digital Subscriptions", "Shopping", "Utilities", "Other"
    )

    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) BentoBgDark else BentoSurfaceLight
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoPrimary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Store") },
                    placeholder = { Text("e.g. Starbucks, Netflix") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Amount ($)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Specify items, gifts, labels") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = {
                            val amount = amountString.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amount > 0.0) {
                                onAddExpense(title.trim(), amount, category, notes.trim())
                            }
                        },
                        enabled = title.isNotBlank() && (amountString.toDoubleOrNull() ?: 0.0) > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BentoSparkline(expenses: List<Expense>, isDark: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (expenses.isEmpty()) {
            // Draw dummy flat sparkline
            drawLine(
                color = if (isDark) Color(0xFF06B6D4) else BentoPrimary,
                start = Offset(0f, height * 0.5f),
                end = Offset(width, height * 0.5f),
                strokeWidth = 3f
            )
            return@Canvas
        }

        val reversed = expenses.reversed()
        val sizeMax = reversed.size
        val points = mutableListOf<Offset>()

        val xDelta = if (sizeMax > 1) width / (sizeMax - 1) else width
        val amounts = reversed.map { it.amount }
        val maxAmount = amounts.maxOrNull() ?: 1.0
        val minAmount = amounts.minOrNull() ?: 0.0
        val range = if (maxAmount - minAmount == 0.0) 1.0 else maxAmount - minAmount

        reversed.forEachIndexed { idx, item ->
            val x = idx * xDelta
            // scale y from 10% bottom padding to 90% top padding
            val yNorm = (item.amount - minAmount) / range
            val y = height - (yNorm * (height * 0.8f) + height * 0.1f).toFloat()
            points.add(Offset(x, y))
        }

        // Generate smooth path
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val controlX = (pPrev.x + pCurr.x) / 2
                    cubicTo(controlX, pPrev.y, controlX, pCurr.y, pCurr.x, pCurr.y)
                }
            }
        }

        // Draw the main stroke
        drawPath(
            path = path,
            color = if (isDark) Color(0xFF06B6D4) else BentoPrimary,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw gradient area below path
        if (points.isNotEmpty()) {
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, height)
                lineTo(points.first().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        (if (isDark) Color(0xFF06B6D4) else BentoPrimary).copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
        }
    }
}
