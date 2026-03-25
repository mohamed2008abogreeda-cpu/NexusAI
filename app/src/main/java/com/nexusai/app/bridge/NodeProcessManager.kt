package com.nexusai.app.bridge

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class NodeState { STOPPED, STARTING, RUNNING, ERROR }

/**
 * Manages the lifecycle of the internal Node.js engine and the Gemini CLI process.
 * Acts as the supervisor coordinating `nodejs-mobile-android`.
 */
@Singleton
class NodeProcessManager @Inject constructor(
    private val ipc: NodeIPC
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _nodeState = MutableStateFlow(NodeState.STOPPED)
    val nodeState: StateFlow<NodeState> = _nodeState

    /**
     * Initializes the Node.js thread, extracts native assets, and boots `npx @google/gemini-cli`.
     */
    fun startRuntime(baseDir: File) {
        if (_nodeState.value == NodeState.RUNNING) return
        _nodeState.value = NodeState.STARTING

        scope.launch {
            try {
                // Asset Extraction logic (simulated)
                // extractAssets(baseDir)
                
                // JNI Call to start Node.js main thread
                // NodeJSMobile.startNodeWithArguments(arrayOf("node", "gemini-cli-entry.js"))
                
                _nodeState.value = NodeState.RUNNING
                
                // Listen to IPC stdout flows via node-bridge
                ipc.cliOutputFlow.collect { rawOutput ->
                    parseStdout(rawOutput)
                }

            } catch (e: Exception) {
                _nodeState.value = NodeState.ERROR
            }
        }
    }

    /**
     * Halts the subprocess to preserve battery when the app is in prolonged background state.
     */
    fun stopRuntime() {
        if (_nodeState.value == NodeState.RUNNING) {
            // Signal graceful shutdown to Node process
            ipc.sendCommand("EXIT")
            _nodeState.value = NodeState.STOPPED
        }
    }

    /**
     * Parses the streaming plaintext output from the terminal. 
     * Applies markdown logic or forwards directly to UI streams.
     */
    private fun parseStdout(chunk: String) {
        // Complex tokenization or simple buffer push for markdown parser
    }
}
