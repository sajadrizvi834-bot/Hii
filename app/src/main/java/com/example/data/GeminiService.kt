package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateFinancialAdvice(
        expenses: List<Expense>,
        blockedItems: List<BlockedItem>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is placeholder or empty. Falling back to local advice generator.")
            return@withContext generateLocalAdvice(expenses, blockedItems)
        }

        val prompt = buildPrompt(expenses, blockedItems)
        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API error: ${response.code} $errorBody")
                    return@withContext generateLocalAdvice(expenses, blockedItems)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
                return@withContext "No response text received from Gemini API."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call: ${e.message}", e)
            return@withContext generateLocalAdvice(expenses, blockedItems)
        }
    }

    private fun buildPrompt(expenses: List<Expense>, blockedItems: List<BlockedItem>): String {
        val totalSpent = expenses.sumOf { it.amount }
        val categoryBreakdown = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val breakdownStr = categoryBreakdown.entries.joinToString("\n") { "- ${it.key}: $${String.format("%.2f", it.value)}" }
        val activeBlocksStr = blockedItems.filter { it.isBlocked }.joinToString(", ") { it.name }

        return """
            You are an expert personal finance coach. Analyze the following spending habits and provide a concise, high-impact, actionable 3-sentence summary of saving tips, followed by a bullet-point list of 3 creative budgeting strategies tailored to this user.
            
            USER FINANCIAL STATUS:
            - Total Expenses Tracked: $${String.format("%.2f", totalSpent)}
            - Expense Category Breakdown:
            $breakdownStr
            - Active Content/Subscription Blocks: $activeBlocksStr
            
            Structure your response exactly like this (plain text, no bold headings):
            [Advice]
            Provide the 3-sentence coaching paragraph here.
            
            [Strategies]
            - Strategy 1
            - Strategy 2
            - Strategy 3
        """.trimIndent()
    }

    fun generateLocalAdvice(expenses: List<Expense>, blockedItems: List<BlockedItem>): String {
        val totalSpent = expenses.sumOf { it.amount }
        val categoryBreakdown = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val activeBlocks = blockedItems.filter { it.isBlocked }
        val topCategory = categoryBreakdown.maxByOrNull { it.value }?.key ?: "N/A"

        val summary = if (expenses.isEmpty()) {
            "You haven't added any expenses yet! Start tracking to unlock smart insights."
        } else {
            "You have tracked $${String.format("%.2f", totalSpent)} in expenses, with your highest spending in $topCategory. " +
            "You currently have ${activeBlocks.size} subscription blocks active to combat impulse buying. " +
            "This proactive approach is helping you build an intentional spending barrier."
        }

        return """
            [Advice]
            $summary Fine-tuning your daily micro-transactions can free up unexpected surplus. Always verify if a purchase serves long-term satisfaction or temporary gratification.
            
            [Strategies]
            - **The 24-Hour Wait Rule**: Put a 24-hour hold on any shopping cart items. 80% of online impulse desires disappear after one sleep cycle.
            - **Audit Digital Services**: Scroll through your subscription blocks list and toggle off unneeded memberships. Standardizing your subscriptions can save up to $150 quarterly.
            - **Category Cap Strategy**: For your largest category ($topCategory), set a weekly target of 15% less than your current average. Put the difference in high-yield vaults.
        """.trimIndent()
    }
}
