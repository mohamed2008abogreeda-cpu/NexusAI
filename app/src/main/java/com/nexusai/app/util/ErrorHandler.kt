package com.nexusai.app.util

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class NodeRuntimeError(val code: Int, val payload: String) : AppError()
    data class DatabaseEncryptionError(val isFatal: Boolean) : AppError()
    data class Unknown(val e: Throwable) : AppError()
}

/**
 * Global Error Handler using Singleton Flow.
 * Allows the UI to cleanly observe and render snackbars or fallback states
 * without crashing the native Node.js Engine.
 */
@Singleton
class ErrorHandler @Inject constructor() {

    private val _errorFlow = MutableSharedFlow<AppError>(extraBufferCapacity = 10)
    val errorFlow: SharedFlow<AppError> = _errorFlow

    fun emitError(error: AppError) {
        val success = _errorFlow.tryEmit(error)
        logError(error)
        if (!success) {
            Log.e("ErrorHandler", "Failed to emit error to UI flow: \$error")
        }
    }

    private fun logError(error: AppError) {
        when (error) {
            is AppError.NetworkError -> Log.e("NexusAI_NET", error.message)
            is AppError.NodeRuntimeError -> Log.e("NexusAI_NODE", "Process crash [\${error.code}]: \${error.payload}")
            is AppError.DatabaseEncryptionError -> Log.e("NexusAI_SEC", "Decryption failed. Fatal = \${error.isFatal}")
            is AppError.Unknown -> Log.e("NexusAI_ERR", "Unknown error occurred", error.e)
        }
    }
}
