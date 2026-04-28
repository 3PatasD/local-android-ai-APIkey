package me.bechberger.phoneserver.security

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.security.SecureRandom
import java.util.*

data class APIKey(
    val id: String = UUID.randomUUID().toString(),
    val key: String = generateRandomKey(),
    val name: String = "API Key",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val isActive: Boolean = true
) {
    companion object {
        fun generateRandomKey(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val random = SecureRandom()
            val prefix = "sk_"
            val random_part = (1..32).map { chars[random.nextInt(chars.length)] }.joinToString("")
            return prefix + random_part
        }
    }
}

class APIKeyManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("api_keys_storage", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyListType = object : TypeToken<List<APIKey>>() {}.type

    fun generateNewKey(name: String): APIKey {
        val newKey = APIKey(
            key = APIKey.generateRandomKey(),
            name = name,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        val keys = getAllKeys().toMutableList()
        keys.add(newKey)
        saveKeys(keys)

        Timber.d("Generated new API key: ${newKey.id}")
        return newKey
    }

    fun validateKey(key: String): Boolean {
        if (!key.startsWith("sk_")) {
            return false
        }

        val apiKey = getAllKeys().find { it.key == key && it.isActive }
        if (apiKey != null) {
            updateLastUsedTime(apiKey.id)
            return true
        }
        return false
    }

    fun getAllKeys(): List<APIKey> {
        val keysJson = sharedPrefs.getString("api_keys_list", "[]") ?: "[]"
        return try {
            gson.fromJson(keysJson, keyListType) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to deserialize API keys")
            emptyList()
        }
    }

    fun getKey(id: String): APIKey? {
        return getAllKeys().find { it.id == id }
    }

    fun revokeKey(id: String): Boolean {
        val keys = getAllKeys().toMutableList()
        val keyIndex = keys.indexOfFirst { it.id == id }

        return if (keyIndex != -1) {
            val revokedKey = keys[keyIndex].copy(isActive = false)
            keys[keyIndex] = revokedKey
            saveKeys(keys)
            Timber.d("Revoked API key: $id")
            true
        } else {
            false
        }
    }

    fun deleteKey(id: String): Boolean {
        val keys = getAllKeys().toMutableList()
        val removed = keys.removeIf { it.id == id }

        return if (removed) {
            saveKeys(keys)
            Timber.d("Deleted API key: $id")
            true
        } else {
            false
        }
    }

    fun updateKeyName(id: String, newName: String): Boolean {
        val keys = getAllKeys().toMutableList()
        val keyIndex = keys.indexOfFirst { it.id == id }

        return if (keyIndex != -1) {
            keys[keyIndex] = keys[keyIndex].copy(name = newName)
            saveKeys(keys)
            Timber.d("Updated API key name: $id")
            true
        } else {
            false
        }
    }

    fun getKeyStats(): Map<String, Any> {
        val keys = getAllKeys()
        return mapOf(
            "totalKeys" to keys.size,
            "activeKeys" to keys.count { it.isActive },
            "revokedKeys" to keys.count { !it.isActive },
            "recentlyUsedKeys" to keys
                .filter { it.lastUsedAt != null }
                .sortedByDescending { it.lastUsedAt }
                .take(5)
                .map { mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "lastUsedAt" to it.lastUsedAt,
                    "createdAt" to it.createdAt
                )}
        )
    }

    private fun updateLastUsedTime(id: String) {
        val keys = getAllKeys().toMutableList()
        val keyIndex = keys.indexOfFirst { it.id == id }

        if (keyIndex != -1) {
            keys[keyIndex] = keys[keyIndex].copy(lastUsedAt = System.currentTimeMillis())
            saveKeys(keys)
        }
    }

    private fun saveKeys(keys: List<APIKey>) {
        val keysJson = gson.toJson(keys)
        sharedPrefs.edit().apply {
            putString("api_keys_list", keysJson)
            apply()
        }
    }
}
