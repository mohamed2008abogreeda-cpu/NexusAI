package com.nexusai.app.bridge

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Handles IPC (Inter-Process Communication) between Kotlin and the Node.js runtime.
 * Implements a Unix Domain Socket or JNI bridge to funnel CLI stdin/stdout.
 */
class NodeIPC {

    // Events emitted from Node.js standard output/error
    private val _cliOutputFlow = MutableSharedFlow<String>()
    val cliOutputFlow: SharedFlow<String> = _cliOutputFlow

    /**
     * Sends a command (e.g., natural language text or raw command) to the internal Node.js runtime.
     */
    fun sendCommand(command: String) {
        // Write to JNI or Socket
        // Example: JniBridge.writeToStdin(command)
    }

    /**
     * Internal callback invoked by JNI when native Node emits data.
     */
    internal suspend fun onDataReceivedFromNode(data: String) {
        _cliOutputFlow.emit(data)
    }
}
