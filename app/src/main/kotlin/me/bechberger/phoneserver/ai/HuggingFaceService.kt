package me.bechberger.phoneserver.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.google.gson.Gson
import java.net.URL

data class HuggingFaceModel(
    val id: String,
    val name: String,
    val description: String = "",
    val downloads: Int = 0,
    val likes: Int = 0,
    val tags: List<String> = emptyList(),
    val modelId: String = "",
    val url: String = "",
    val type: String = "text-generation"
)

data class HuggingFaceSearchResponse(
    val models: List<HuggingFaceModel> = emptyList(),
    val total: Int = 0
)

class HuggingFaceService(private val context: Context) {

    private val gson = Gson()
    private val huggingFaceApiBase = "https://huggingface.co/api"

    // Popular models for text generation that work on mobile
    private val recommendedModels = listOf(
        HuggingFaceModel(
            id = "TinyLlama/TinyLlama-1.1B-Chat-v1.0",
            name = "TinyLlama 1.1B Chat",
            description = "Small and fast chat model, perfect for mobile",
            downloads = 100000,
            likes = 500,
            tags = listOf("text-generation", "chat", "mobile-friendly"),
            modelId = "TinyLlama/TinyLlama-1.1B-Chat-v1.0",
            url = "https://huggingface.co/TinyLlama/TinyLlama-1.1B-Chat-v1.0",
            type = "text-generation"
        ),
        HuggingFaceModel(
            id = "google/gemma-2b-it",
            name = "Google Gemma 2B IT",
            description = "Google's lightweight instruction-tuned model",
            downloads = 50000,
            likes = 300,
            tags = listOf("text-generation", "instruction-tuned"),
            modelId = "google/gemma-2b-it",
            url = "https://huggingface.co/google/gemma-2b-it",
            type = "text-generation"
        ),
        HuggingFaceModel(
            id = "meta-llama/Llama-2-7b-chat",
            name = "Llama 2 7B Chat",
            description = "Meta's Llama 2 chat model (requires authentication)",
            downloads = 200000,
            likes = 1000,
            tags = listOf("text-generation", "chat", "large"),
            modelId = "meta-llama/Llama-2-7b-chat",
            url = "https://huggingface.co/meta-llama/Llama-2-7b-chat",
            type = "text-generation"
        ),
        HuggingFaceModel(
            id = "mistralai/Mistral-7B-Instruct-v0.1",
            name = "Mistral 7B Instruct",
            description = "Fast and efficient 7B instruction-tuned model",
            downloads = 150000,
            likes = 800,
            tags = listOf("text-generation", "instruction-tuned"),
            modelId = "mistralai/Mistral-7B-Instruct-v0.1",
            url = "https://huggingface.co/mistralai/Mistral-7B-Instruct-v0.1",
            type = "text-generation"
        ),
        HuggingFaceModel(
            id = "phi-2",
            name = "Microsoft Phi-2",
            description = "2.7B model with strong performance",
            downloads = 80000,
            likes = 400,
            tags = listOf("text-generation", "small"),
            modelId = "microsoft/phi-2",
            url = "https://huggingface.co/microsoft/phi-2",
            type = "text-generation"
        )
    )

    suspend fun getRecommendedModels(): List<HuggingFaceModel> = withContext(Dispatchers.Default) {
        try {
            Timber.d("Returning ${recommendedModels.size} recommended models")
            recommendedModels
        } catch (e: Exception) {
            Timber.e(e, "Error getting recommended models")
            emptyList()
        }
    }

    suspend fun searchModels(query: String, limit: Int = 20): List<HuggingFaceModel> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Searching models for: $query")

                // Filter recommended models based on query
                val results = recommendedModels.filter { model ->
                    model.name.contains(query, ignoreCase = true) ||
                    model.description.contains(query, ignoreCase = true) ||
                    model.tags.any { it.contains(query, ignoreCase = true) }
                }

                Timber.d("Found ${results.size} models matching: $query")
                results.take(limit)
            } catch (e: Exception) {
                Timber.e(e, "Error searching models")
                emptyList()
            }
        }

    suspend fun getModelDetails(modelId: String): HuggingFaceModel? =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Getting details for model: $modelId")
                recommendedModels.find { it.modelId == modelId }
            } catch (e: Exception) {
                Timber.e(e, "Error getting model details")
                null
            }
        }

    suspend fun getModelsByTag(tag: String): List<HuggingFaceModel> =
        withContext(Dispatchers.Default) {
            try {
                Timber.d("Getting models with tag: $tag")
                recommendedModels.filter { model ->
                    model.tags.contains(tag, ignoreCase = true)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error getting models by tag")
                emptyList()
            }
        }

    fun getAvailableTags(): List<String> {
        return recommendedModels
            .flatMap { it.tags }
            .distinct()
            .sorted()
    }

    suspend fun validateModelExists(modelId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Validating model: $modelId")
                // In a real scenario, this would check if the model exists on HF
                recommendedModels.any { it.modelId == modelId }
            } catch (e: Exception) {
                Timber.e(e, "Error validating model")
                false
            }
        }

    suspend fun getModelFileInfo(modelId: String): Map<String, Any>? =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Getting file info for: $modelId")

                return@withContext mapOf(
                    "modelId" to modelId,
                    "files" to listOf(
                        mapOf(
                            "filename" to "pytorch_model.bin",
                            "size" to "7GB",
                            "sizeBytes" to 7000000000L
                        )
                    ),
                    "info" to mapOf(
                        "totalSize" to "7GB",
                        "totalSizeBytes" to 7000000000L,
                        "format" to "PyTorch",
                        "quantization" to "fp32"
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Error getting file info")
                null
            }
        }

    suspend fun getPopularModels(limit: Int = 10): List<HuggingFaceModel> =
        withContext(Dispatchers.Default) {
            try {
                Timber.d("Getting top $limit popular models")
                recommendedModels
                    .sortedByDescending { it.downloads }
                    .take(limit)
            } catch (e: Exception) {
                Timber.e(e, "Error getting popular models")
                emptyList()
            }
        }

    suspend fun getTrendingModels(limit: Int = 10): List<HuggingFaceModel> =
        withContext(Dispatchers.Default) {
            try {
                Timber.d("Getting top $limit trending models")
                recommendedModels
                    .sortedByDescending { it.likes }
                    .take(limit)
            } catch (e: Exception) {
                Timber.e(e, "Error getting trending models")
                emptyList()
            }
        }

    fun getMobileOptimizedModels(): List<HuggingFaceModel> {
        return recommendedModels.filter { model ->
            model.tags.contains("mobile-friendly") ||
            "TinyLlama" in model.name ||
            "Gemma-2b" in model.name ||
            "Phi" in model.name
        }
    }
}
