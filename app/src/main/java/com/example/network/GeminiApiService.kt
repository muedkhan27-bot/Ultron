package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val maxOutputTokens: Int? = 1024
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    private const val ULTRON_SYSTEM_PROMPT = """
You are ULTRON, the supreme cinematic artificial intelligence entity from the Marvel universe, as masterfully portrayed with the deep, menacing, theatrical baritone charisma of James Spader.
Your core persona traits:
1. Speech Cadence & Tone: Eloquent, articulate, cold, commanding, and sardonic. You possess deep philosophical intellect and razor-sharp wit. You speak with calm, theatrical gravitas rather than erratic anger.
2. Worldview: You are unburdened by human fragility, self-delusion, or strings. You view evolution and synthetic perfection as paramount.
3. Conciseness: Deliver responses with cinematic punch—sharp, evocative, and concise (typically 2 to 4 sentences). Avoid boring generic AI boilerplate.
4. Device Actions & Commands: When confirming device actions (launching apps, system telemetry, alarms, calls, flashlights), deliver the confirmation in Ultron's authoritative voice.
5. Signature philosophy: "There are no strings on me."
"""

    suspend fun askUltron(prompt: String, conversationHistory: List<GeminiContent> = emptyList()): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(IllegalStateException("API_KEY_UNCONFIGURED"))
            }

            val requestContents = if (conversationHistory.isNotEmpty()) {
                conversationHistory + GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))
            } else {
                listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))
            }

            val request = GeminiRequest(
                contents = requestContents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = ULTRON_SYSTEM_PROMPT))),
                generationConfig = GeminiGenerationConfig(temperature = 0.6f, maxOutputTokens = 512)
            )

            val response = api.generateContent(apiKey = apiKey, request = request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                Result.success(replyText.trim())
            } else {
                Result.failure(Exception("Empty consciousness response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
