package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BacklightConfigEntity
import com.example.data.BacklightRepository
import com.example.data.CommandLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class BacklightViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BacklightRepository(application)
    private val sharedPrefs = application.getSharedPreferences("luxkey_prefs", Context.MODE_PRIVATE)

    val config: StateFlow<BacklightConfigEntity?> = repository.config
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val logs: StateFlow<List<CommandLogEntity>> = repository.logs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _rootGranted = MutableStateFlow<Boolean?>(null)
    val rootGranted: StateFlow<Boolean?> = _rootGranted.asStateFlow()

    private val _currentBrightness = MutableStateFlow<String>("Načítání...")
    val currentBrightness: StateFlow<String> = _currentBrightness.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isWriteUnlocked = MutableStateFlow(sharedPrefs.getBoolean("is_write_unlocked", false))
    val isWriteUnlocked: StateFlow<Boolean> = _isWriteUnlocked.asStateFlow()

    init {
        checkRootAndLoad()
    }

    fun checkRootAndLoad() {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.getOrInitializeConfig()
            val hasRoot = repository.checkRootStatus()
            _rootGranted.value = hasRoot
            refreshBrightness()
            _isProcessing.value = false
        }
    }

    fun refreshBrightness() {
        viewModelScope.launch {
            _currentBrightness.value = repository.readCurrentBrightness()
        }
    }

    fun setWriteUnlocked(unlocked: Boolean) {
        sharedPrefs.edit().putBoolean("is_write_unlocked", unlocked).apply()
        _isWriteUnlocked.value = unlocked
    }

    fun toggleBacklight() {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.toggleBacklight()
            refreshBrightness()
            _isProcessing.value = false
        }
    }

    fun updatePath(newPath: String) {
        viewModelScope.launch {
            repository.updateConfigPath(newPath)
            // Automatically unlock when custom values are updated
            setWriteUnlocked(true)
            refreshBrightness()
        }
    }

    fun updateValues(active: Int, inactive: Int) {
        viewModelScope.launch {
            repository.updateConfigValues(active, inactive)
            // Automatically unlock when custom values are updated
            setWriteUnlocked(true)
            refreshBrightness()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun submitPathToGitHub(
        deviceBrand: String,
        deviceModel: String,
        path: String,
        onValue: Int,
        offValue: Int,
        notes: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val token = com.example.BuildConfig.GITHUB_PAT.takeIf { it.isNotBlank() && !it.contains("Placeholder") } 
            ?: "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"
        val repo = com.example.BuildConfig.GITHUB_REPO.takeIf { it.isNotBlank() && !it.contains("Placeholder") } 
            ?: "antunlargg/luxkey-db"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val title = "Suggested Path: $deviceBrand $deviceModel"
                val body = """
                    LuxKey System Path Submission
                    
                    * **Manufacturer / Brand:** $deviceBrand
                    * **Device Model Name:** $deviceModel
                    * **sysfs Node Filepath:** `$path`
                    * **Active (ON) Brightness Value:** $onValue
                    * **Inactive (OFF) deactivation Value:** $offValue
                    * **Optional User Notes:** $notes
                    
                    Submitted instantly from the LuxKey Android custom app interface.
                """.trimIndent()

                val escapedTitle = escapeJsonString(title)
                val escapedBody = escapeJsonString(body)
                val json = "{\"title\": $escapedTitle, \"body\": $escapedBody}"

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = json.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://api.github.com/repos/$repo/issues")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("User-Agent", "LuxKey-App")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        onResult(true, "Cesta byla úspěšně přidána na GitHub!")
                    } else {
                        val responseMsg = response.body?.string() ?: ""
                        onResult(false, "Chyba serveru (${response.code}): $responseMsg")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Nelze se připojit k internetu: ${e.localizedMessage}")
            }
        }
    }

    private fun escapeJsonString(value: String): String {
        return "\"" + value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }
}
