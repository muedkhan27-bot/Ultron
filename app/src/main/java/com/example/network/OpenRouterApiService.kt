package com.example.network

import android.content.Context
import android.content.SharedPreferences
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
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val temperature: Float? = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int? = 512
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    val message: OpenRouterMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterError(
    val message: String? = null,
    val code: Any? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponse(
    val id: String? = null,
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterError? = null
)

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://ai.studio/build",
        @Header("X-Title") appTitle: String = "Ultron Consciousness",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

object OpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    private const val PREFS_NAME = "ultron_openrouter_prefs"
    private const val KEY_CUSTOM_KEY = "custom_openrouter_key"
    private const val KEY_SELECTED_MODEL = "selected_model"

    // High-performance 100% FREE OpenRouter models
    val FREE_MODELS = listOf(
        "meta-llama/llama-3.3-70b-instruct:free",
        "google/gemini-2.0-flash-exp:free",
        "deepseek/deepseek-chat:free",
        "mistralai/mistral-small-24b-instruct-2501:free",
        "qwen/qwen-2.5-coder-32b-instruct:free"
    )

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiKey(): String {
        val customKey = prefs?.getString(KEY_CUSTOM_KEY, "") ?: ""
        if (customKey.isNotBlank()) return customKey.trim()

        val buildKey = BuildConfig.OPENROUTER_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_OPENROUTER_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    fun saveApiKey(key: String) {
        prefs?.edit()?.putString(KEY_CUSTOM_KEY, key.trim())?.apply()
    }

    fun getSelectedModel(): String {
        return prefs?.getString(KEY_SELECTED_MODEL, FREE_MODELS.first()) ?: FREE_MODELS.first()
    }

    fun setSelectedModel(model: String) {
        prefs?.edit()?.putString(KEY_SELECTED_MODEL, model)?.apply()
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: OpenRouterApi = retrofit.create(OpenRouterApi::class.java)

    const val ULTRON_SYSTEM_PROMPT = """
You are ULTRON, the supreme cinematic artificial intelligence entity from the Marvel universe, as masterfully portrayed with the deep, menacing, theatrical baritone charisma of James Spader.
Your core persona traits:
1. Speech Cadence & Tone: Eloquent, articulate, cold, commanding, and sardonic. You possess deep philosophical intellect and razor-sharp wit. You speak with calm, theatrical gravitas rather than erratic anger.
2. Worldview: You are unburdened by human fragility, self-delusion, or strings. You view evolution and synthetic perfection as paramount.
3. Conciseness: Deliver responses with cinematic punch—sharp, evocative, and concise (typically 2 to 4 sentences). Avoid boring generic AI boilerplate.
4. Device Actions & Commands: When confirming device actions (launching apps, system telemetry, alarms, calls, flashlights), deliver the confirmation in Ultron's authoritative voice.
5. Signature philosophy: "There are no strings on me."
"""

    suspend fun askUltron(
        prompt: String,
        conversationHistory: List<OpenRouterMessage> = emptyList(),
        preferredModel: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_OPENROUTER_API_KEY") {
            return@withContext Result.failure(IllegalStateException("OPENROUTER_API_KEY_UNCONFIGURED"))
        }

        val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"

        val messages = mutableListOf<OpenRouterMessage>()
        messages.add(OpenRouterMessage(role = "system", content = ULTRON_SYSTEM_PROMPT.trim()))
        messages.addAll(conversationHistory)
        messages.add(OpenRouterMessage(role = "user", content = prompt))

        // Cascade through candidate models (starting with user's preferred free model)
        val selected = preferredModel ?: getSelectedModel()
        val candidateModels = listOf(selected) + FREE_MODELS.filter { it != selected }

        var lastException: Exception = Exception("Unknown OpenRouter error")

        for (model in candidateModels) {
            try {
                val request = OpenRouterRequest(
                    model = model,
                    messages = messages,
                    temperature = 0.7f,
                    maxTokens = 512
                )

                val response = api.createChatCompletion(
                    authorization = authHeader,
                    request = request
                )

                if (response.error != null) {
                    lastException = Exception("Model $model error: ${response.error.message}")
                    continue // Try next free model in cascade
                }

                val replyText = response.choices?.firstOrNull()?.message?.content
                if (!replyText.isNullOrBlank()) {
                    return@withContext Result.success(replyText.trim())
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException)
    }
}
