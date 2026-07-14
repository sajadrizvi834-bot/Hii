package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BlockedItem
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.*

@Composable
fun SuggestedFeedScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val adviceText by viewModel.adviceText.collectAsState()
    val isGeneratingAdvice by viewModel.isGeneratingAdvice.collectAsState()
    val blockedItems by viewModel.blockedItems.collectAsState()

    var showAddBlockerDialog by remember { mutableStateOf(false) }
    var selectedStoryForDialog by remember { mutableStateOf<FinancialStory?>(null) }

    val isDark = isSystemInDarkTheme()

    // Parse advice text from VM (advice & strategies)
    val parsedAdvice = remember(adviceText) {
        parseAdvice(adviceText)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Column {
                    Text(
                        text = "Smart Coach",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Intelligent AI feedback & spending shields",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Daily FinTips Stories Bubble Row
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) BentoSurfaceDark else BentoSurfaceLight
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Daily FinTips Stories",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 0.5.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(getFinancialStories()) { story ->
                                StoryBubble(
                                    story = story,
                                    onClick = { selectedStoryForDialog = story }
                                )
                            }
                        }
                    }
                }
            }

            // AI/Local Smart Advice Board - Large Lavender Bento Box
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) BentoSurfaceDark else BentoLavender.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) BentoBorderDark else BentoLavenderText.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Coach",
                                    tint = if (isDark) BentoSecondary else BentoPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "AI Guard Advisor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else BentoPrimary
                                )
                            }

                            // Refresh button
                            IconButton(
                                onClick = { viewModel.generateAdvice() },
                                enabled = !isGeneratingAdvice,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isDark) Color.White.copy(alpha = 0.1f) else BentoPrimary.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                            ) {
                                if (isGeneratingAdvice) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = if (isDark) BentoSecondary else BentoPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Advice",
                                        tint = if (isDark) Color.White else BentoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (isGeneratingAdvice) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Analyzing expenses with Gemini AI...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color.White.copy(alpha = 0.5f) else BentoLavenderText.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            // Section: Coaching advice text
                            Text(
                                text = parsedAdvice.first,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp,
                                color = if (isDark) Color.White else BentoLavenderText,
                                fontWeight = FontWeight.Medium
                            )

                            HorizontalDivider(
                                color = if (isDark) BentoBorderDark else BentoLavenderText.copy(alpha = 0.1f)
                            )

                            // Section: Actionable strategies
                            Text(
                                text = "Actionable Strategies:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) BentoSecondary else BentoPrimary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                parsedAdvice.second.forEach { strategy ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isDark) BentoSecondary else BentoPrimary,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Text(
                                            text = strategy,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Color.White.copy(alpha = 0.8f) else BentoLavenderText.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Spending Blocks / Guards Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Shield Guards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Trigger alerts for non-essential outflows",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Button(
                        onClick = { showAddBlockerDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Guard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Active list of blockers with toggle switches
            if (blockedItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) BentoSurfaceDark else BentoSurfaceLight
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GppGood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Shield protection offline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure active guards to trigger cautionary prompts when recording high-risk impulse or subscription transactions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            } else {
                items(blockedItems) { item ->
                    BlockerItemRow(
                        item = item,
                        onToggle = { viewModel.toggleBlockStatus(item) },
                        onRemove = { viewModel.removeBlockedItem(item) }
                    )
                }
            }
        }
    }

    // Modal to add a custom blocker (Bento overlay style)
    if (showAddBlockerDialog) {
        AddBlockerDialog(
            onDismiss = { showAddBlockerDialog = false },
            onAddBlocker = { name, category, redirect ->
                viewModel.addCustomBlock(name, category, redirect)
                showAddBlockerDialog = false
            }
        )
    }

    // Story Detail Dialog (styled modern bento stories)
    selectedStoryForDialog?.let { story ->
        Dialog(onDismissRequest = { selectedStoryForDialog = null }) {
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
                Column {
                    // Story Header visual block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        getStoryColor(story.themeId),
                                        getStoryColor(story.themeId).copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                textBlock(
                                    text = story.tag,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = story.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Story Body details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = story.content,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Quote board
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) BentoSurfaceDark else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isDark) BentoSecondary else BentoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = story.takeaway,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = { selectedStoryForDialog = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                        ) {
                            Text("Apply Strategy", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun textBlock(text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight) {
    Text(text = text, color = color, fontSize = fontSize, fontWeight = fontWeight)
}

@Composable
fun StoryBubble(
    story: FinancialStory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        // Glowing story border
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            getStoryColor(story.themeId),
                            getStoryColor(story.themeId).copy(alpha = 0.5f),
                            BentoPrimary.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (isDark) BentoSurfaceDark else Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = story.icon,
                    contentDescription = null,
                    tint = getStoryColor(story.themeId),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = story.shortTitle,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun BlockerItemRow(
    item: BlockedItem,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isBlocked) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isBlocked) Icons.Default.GppBad else Icons.Default.GppGood,
                        contentDescription = null,
                        tint = if (item.isBlocked) MaterialTheme.colorScheme.error else (if (isDark) BentoSecondary else BentoPrimary),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.category} guard",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Toggle switch
                Switch(
                    checked = item.isBlocked,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.scale(0.82f)
                )

                // Remove Guard Button
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Guard",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddBlockerDialog(
    onDismiss: () -> Unit,
    onAddBlocker: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Social Media Ads") }
    var redirect by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Social Media Ads", "Digital Subscriptions", "Shopping Platforms", "Other Non-Essential")
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
                    text = "Add Spending Guard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App or Subscription Name") },
                    placeholder = { Text("e.g. TikTok, Spotify, Netflix") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Dropdown Category
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Guard Category") },
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
                    value = redirect,
                    onValueChange = { redirect = it },
                    label = { Text("Guard Warning Text (Optional)") },
                    placeholder = { Text("e.g. Stop wasting hours!") },
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
                            if (name.isNotBlank()) {
                                onAddBlocker(name.trim(), category, redirect.trim())
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set Block", color = Color.White)
                    }
                }
            }
        }
    }
}

// Extension to scale switches down nicely
private fun Modifier.scale(scale: Float) = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val scaledWidth = (placeable.width * scale).toInt()
        val scaledHeight = (placeable.height * scale).toInt()
        layout(scaledWidth, scaledHeight) {
            placeable.placeRelative(
                ((scaledWidth - placeable.width) / 2),
                ((scaledHeight - placeable.height) / 2)
            )
        }
    }
)

// Helpers to parse advice string from service
private fun parseAdvice(rawAdviceText: String): Pair<String, List<String>> {
    if (rawAdviceText.isBlank()) {
        return "Start tracking your personal expenses to generate smart insights!" to emptyList()
    }

    return try {
        val adviceTag = "[Advice]"
        val strategiesTag = "[Strategies]"

        val adviceIndex = rawAdviceText.indexOf(adviceTag)
        val strategiesIndex = rawAdviceText.indexOf(strategiesTag)

        val advice = if (adviceIndex != -1) {
            val start = adviceIndex + adviceTag.length
            val end = if (strategiesIndex != -1 && strategiesIndex > start) strategiesIndex else rawAdviceText.length
            rawAdviceText.substring(start, end).trim()
        } else {
            rawAdviceText
        }

        val strategiesList = mutableListOf<String>()
        if (strategiesIndex != -1) {
            val strategiesText = rawAdviceText.substring(strategiesIndex + strategiesTag.length).trim()
            strategiesText.split("\n").forEach { line ->
                val cleaned = line.replace("^-\\s*\\*?\\*?".toRegex(), "").replace("\\*?\\*?$".toRegex(), "").trim()
                if (cleaned.isNotBlank()) {
                    strategiesList.add(cleaned)
                }
            }
        }

        if (strategiesList.isEmpty() && adviceIndex == -1) {
            // Unstructured layout, split by paragraphs
            val paragraphs = rawAdviceText.split("\n\n")
            val p1 = paragraphs.firstOrNull() ?: ""
            val rem = paragraphs.drop(1).flatMap { it.split("\n") }.map { it.replace("^-\\s*".toRegex(), "").trim() }.filter { it.isNotBlank() }
            p1 to rem
        } else {
            advice to strategiesList
        }
    } catch (e: Exception) {
        rawAdviceText to emptyList()
    }
}

data class FinancialStory(
    val title: String,
    val shortTitle: String,
    val icon: ImageVector,
    val tag: String,
    val content: String,
    val takeaway: String,
    val themeId: Int
)

fun getFinancialStories(): List<FinancialStory> {
    return listOf(
        FinancialStory(
            title = "Algorithmic Wallet Traps",
            shortTitle = "Wallet Traps",
            icon = Icons.Outlined.Campaign,
            tag = "PSYCHOLOGY",
            content = "Did you know that social media feeds serve ads tailored to your psychological profile exactly when you are most tired or vulnerable? This is called 'algorithmic prompt friction'. Set a daily content block on shopping ads between 9 PM and 7 AM to automatically break this subconscious feedback loop.",
            takeaway = "85% of ad purchases occur during low-willpower hours.",
            themeId = 1
        ),
        FinancialStory(
            title = "Sub Auditing Hacks",
            shortTitle = "Sub Audits",
            icon = Icons.Outlined.Subscriptions,
            tag = "BUDGETING",
            content = "We subscribe to stay relevant, but 40% of digital streaming memberships go completely unused for 60 consecutive days. The 'Streaming Pause' strategy recommends toggling off Netflix, Spotify, or Disney+ for one full cycle every quarter. If you don't miss them, pocket the cash flow!",
            takeaway = "Unused subs waste an average of ${'$'}240 annually.",
            themeId = 2
        ),
        FinancialStory(
            title = "The Daily Coffee Myth",
            shortTitle = "Micro Leaks",
            icon = Icons.Outlined.Coffee,
            tag = "MICRO-LEAKS",
            content = "It's not about the ${'$'}5 latte; it's about the compound leakage. Spending ${'$'}5 daily adds up to ${'$'}150 monthly. If invested in standard index funds returning 8% over 10 years, that micro-leak becomes over ${'$'}27,000 in wealth. Redirecting micro-leaks to a savings tracker vault builds secure futures.",
            takeaway = "Compound micro-savings generate massive macro-gains.",
            themeId = 3
        )
    )
}

fun getStoryColor(themeId: Int): Color {
    return when (themeId) {
        1 -> Color(0xFFEF4444) // Coral Red
        2 -> Color(0xFF8B5CF6) // Royal Violet
        3 -> Color(0xFF06B6D4) // Bright Teal
        else -> Color(0xFF10B981)
    }
}
