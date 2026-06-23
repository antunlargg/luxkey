package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootShell {
    private const val TAG = "RootShell"

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            process.outputStream.use { os ->
                os.write("exit\n".toByteArray())
                os.flush()
            }
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed: ${e.message}")
            false
        } finally {
            process?.destroy()
        }
    }

    suspend fun executeCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        var process: Process? = null
        var os: DataOutputStream? = null
        var isReader: BufferedReader? = null
        var esReader: BufferedReader? = null
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            
            os.writeBytes(command + "\n")
            os.writeBytes("exit\n")
            os.flush()

            val exitCode = process.waitFor()
            isReader = BufferedReader(InputStreamReader(process.inputStream))
            esReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = StringBuilder()
            var line: String?
            while (isReader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            val error = StringBuilder()
            while (esReader.readLine().also { line = it } != null) {
                error.append(line).append("\n")
            }

            ShellResult(exitCode == 0, output.toString().trim(), error.toString().trim())
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: ${e.message}")
            ShellResult(false, "", e.localizedMessage ?: "Unknown error")
        } finally {
            try { os?.close() } catch (ignored: Exception) {}
            try { isReader?.close() } catch (ignored: Exception) {}
            try { esReader?.close() } catch (ignored: Exception) {}
            process?.destroy()
        }
    }
}

data class ShellResult(
    val success: Boolean,
    val output: String,
    val error: String
)
