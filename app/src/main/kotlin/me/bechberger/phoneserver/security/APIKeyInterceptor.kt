package me.bechberger.phoneserver.security

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import timber.log.Timber

enum class AuthRequirement {
    NONE,           // No authentication required
    OPTIONAL,       // Authentication optional but recommended
    REQUIRED        // Authentication required
}

object APIKeyInterceptor {

    // Map of endpoints and their auth requirements
    private val authRequiredEndpoints = mapOf(
        // AI endpoints require authentication
        "/ai/text" to AuthRequirement.REQUIRED,
        "/ai/object_detection" to AuthRequirement.REQUIRED,
        "/ai/models/download" to AuthRequirement.REQUIRED,
        "/ai/text/upload-model-api" to AuthRequirement.REQUIRED,
        "/ai/models/cleanup" to AuthRequirement.REQUIRED,
        "/ai/models/" to AuthRequirement.REQUIRED,  // DELETE endpoint

        // Public endpoints that don't require auth
        "/status" to AuthRequirement.NONE,
        "/health" to AuthRequirement.NONE,
        "/capabilities" to AuthRequirement.NONE,
        "/location" to AuthRequirement.OPTIONAL,
        "/orientation" to AuthRequirement.OPTIONAL,
        "/capture" to AuthRequirement.OPTIONAL,
        "/display" to AuthRequirement.OPTIONAL,
        "/ai/models" to AuthRequirement.NONE,
        "/ai/models/status" to AuthRequirement.NONE,
        "/ai/models/loading-status" to AuthRequirement.NONE,
        "/ai/models/diagnostics" to AuthRequirement.NONE,
        "/help" to AuthRequirement.NONE
    )

    fun getAuthRequirement(path: String): AuthRequirement {
        // Check exact matches first
        authRequiredEndpoints[path]?.let { return it }

        // Check prefix matches for dynamic routes
        for ((endpoint, requirement) in authRequiredEndpoints) {
            if (endpoint.endsWith("/") && path.startsWith(endpoint)) {
                return requirement
            }
        }

        // Default to optional auth
        return AuthRequirement.OPTIONAL
    }

    suspend fun validateApiKey(
        call: ApplicationCall,
        apiKeyManager: APIKeyManager
    ): Boolean {
        val authHeader = call.request.headers["Authorization"]

        if (authHeader == null) {
            val requirement = getAuthRequirement(call.request.uri)
            if (requirement == AuthRequirement.REQUIRED) {
                Timber.w("Missing API key for protected endpoint: ${call.request.uri}")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "error" to "API key required",
                        "code" to "MISSING_API_KEY",
                        "description" to "This endpoint requires an API key. Include it in the Authorization header: Authorization: Bearer <API_KEY>",
                        "documentation" to "GET /api-keys/help"
                    )
                )
                return false
            }
            return true
        }

        // Extract key from "Bearer <key>" format
        val key = if (authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else if (authHeader.startsWith("sk_")) {
            authHeader
        } else {
            null
        }

        if (key == null) {
            Timber.w("Invalid authorization header format: $authHeader")
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to "Invalid authorization header",
                    "code" to "INVALID_AUTH_FORMAT",
                    "description" to "Authorization header must be in format: Bearer <API_KEY> or just <API_KEY>"
                )
            )
            return false
        }

        if (!apiKeyManager.validateKey(key)) {
            Timber.w("Invalid or revoked API key attempted")
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf(
                    "error" to "Invalid API key",
                    "code" to "INVALID_API_KEY",
                    "description" to "The provided API key is invalid or has been revoked"
                )
            )
            return false
        }

        return true
    }

    suspend fun optionallyValidateApiKey(
        call: ApplicationCall,
        apiKeyManager: APIKeyManager
    ): String? {
        val authHeader = call.request.headers["Authorization"] ?: return null

        val key = if (authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else if (authHeader.startsWith("sk_")) {
            authHeader
        } else {
            return null
        }

        if (apiKeyManager.validateKey(key)) {
            return key
        }

        return null
    }
}
