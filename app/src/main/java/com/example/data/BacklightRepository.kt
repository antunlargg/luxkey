package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BacklightRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val backlightDao = database.backlightDao()
    private val logDao = database.logDao()

    val config: Flow<BacklightConfigEntity?> = backlightDao.getConfig()
    val logs: Flow<List<CommandLogEntity>> = logDao.getRecentLogs()

    suspend fun getOrInitializeConfig(): BacklightConfigEntity {
        val existing = backlightDao.getConfigDirect()
        if (existing == null) {
            val default = BacklightConfigEntity()
            backlightDao.insertConfig(default)
            return default
        }
        return existing
    }

    suspend fun updateConfigPath(filePath: String) {
        val current = getOrInitializeConfig()
        val updated = current.copy(filePath = filePath)
        backlightDao.insertConfig(updated)
        addLog("PATH_CHANGE", "Change path: $filePath", true, "Path updated in local state.")
    }

    suspend fun updateConfigValues(active: Int, inactive: Int) {
        val current = getOrInitializeConfig()
        val updated = current.copy(activeValue = active, inactiveValue = inactive)
        backlightDao.insertConfig(updated)
        addLog("CUSTOM_CHANGE", "Change values: Active=$active, Inactive=$inactive", true, "Brightness parameters updated.")
    }

    suspend fun checkRootStatus(): Boolean {
        val rootOk = RootShell.isRootAvailable()
        addLog(
            actionType = "ROOT_CHECK",
            command = "su (verify access)",
            isSuccess = rootOk,
            details = if (rootOk) "Root privilege is present and granted." else "Root binary not found, or access was explicitly denied."
        )
        return rootOk
    }

    suspend fun toggleBacklight(): Boolean {
        val current = getOrInitializeConfig()
        val targetDeactivatedState = !current.isDeactivated
        return applyBacklightState(targetDeactivatedState, current)
    }

    suspend fun setBacklightDeactivated(deactivate: Boolean): Boolean {
        val current = getOrInitializeConfig()
        return applyBacklightState(deactivate, current)
    }

    private suspend fun applyBacklightState(
        deactivate: Boolean,
        currentConfig: BacklightConfigEntity
    ): Boolean {
        val sharedPrefs = context.getSharedPreferences("luxkey_prefs", Context.MODE_PRIVATE)
        val isWriteUnlocked = sharedPrefs.getBoolean("is_write_unlocked", false)
        if (!isWriteUnlocked) {
            addLog(
                actionType = "WRITE_BLOCKED",
                command = "echo (Blocked by Safety Lock)",
                isSuccess = false,
                details = "Zápis do systému je zablokován, dokud neuložíte vlastní parametry nebo neodblokujete zápis v nastavení."
            )
            return false
        }

        val targetValue = if (deactivate) currentConfig.inactiveValue else currentConfig.activeValue
        val filePath = currentConfig.filePath
        
        // Execute root write command
        // First change permissions to ensure read-write, then echo value to the sysfs path
        val command = "chmod 666 $filePath && echo $targetValue > $filePath"
        val shellRes = RootShell.executeCommand(command)

        val updated = currentConfig.copy(isDeactivated = deactivate)
        backlightDao.insertConfig(updated)

        val actionName = if (deactivate) "DISABLE" else "ENABLE"
        val displayState = if (deactivate) "Backlight deactivation" else "Backlight activation"
        
        addLog(
            actionType = actionName,
            command = command,
            isSuccess = shellRes.success,
            details = if (shellRes.success) {
                "$displayState completed successfully (wrote '$targetValue' to $filePath)."
            } else {
                "Error executing '$targetValue' write to $filePath: ${shellRes.error}"
            }
        )

        return shellRes.success
    }

    suspend fun readCurrentBrightness(): String {
        val current = getOrInitializeConfig()
        val filePath = current.filePath
        val command = "cat $filePath"
        val shellRes = RootShell.executeCommand(command)
        
        if (shellRes.success) {
            val output = shellRes.output.trim()
            if (output.isNotEmpty()) {
                val isDeactivatedNow = (output == current.inactiveValue.toString())
                if (isDeactivatedNow != current.isDeactivated) {
                    val updated = current.copy(isDeactivated = isDeactivatedNow)
                    backlightDao.insertConfig(updated)
                }
            }
            return output
        } else {
            Log.e("BacklightRepository", "Cat failed: ${shellRes.error}")
            return "Unknown"
        }
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    private suspend fun addLog(actionType: String, command: String, isSuccess: Boolean, details: String) {
        logDao.insertLog(
            CommandLogEntity(
                timestamp = System.currentTimeMillis(),
                actionType = actionType,
                command = command,
                isSuccess = isSuccess,
                details = details
            )
        )
    }
}
