package com.nexusai.app.bridge

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry holding wrappers for internal Node.js MCP (Model Context Protocol) 
 * skills and interacting with native Android skills (e.g. Camera, System Settings).
 */
@Singleton
class SkillRegistry @Inject constructor(
    private val ipc: NodeIPC
) {

    // Simulates dynamic loading of downloaded CLI tools
    val availableSkills = mutableListOf<String>()

    init {
        // Core skills mapping to default MCP servers
        availableSkills.addAll(listOf(
            "file-system",
            "google-search",
            "memory-systems",
            "bug-hunter"
        ))
    }

    /**
     * Triggers a specific tool format via IPC to the internal Gemini node process.
     */
    fun invokeSkill(skillName: String, jsonArgs: String) {
        // Dispatch tool call to Node.js
        val command = """
            { "type": "tool_call", "skill": "\$skillName", "args": \$jsonArgs }
        """.trimIndent()
        
        ipc.sendCommand(command)
    }
}
