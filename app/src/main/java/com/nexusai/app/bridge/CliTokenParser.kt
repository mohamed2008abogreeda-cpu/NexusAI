package com.nexusai.app.bridge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Parses the raw stdout string stream from the Gemini CLI into structured UI objects.
 * Identifies Markdown streams, Agent Tool call blocks, and process errors.
 */
class CliTokenParser @Inject constructor() {

    sealed class ParsedToken {
        data class TextChunk(val text: String) : ParsedToken()
        data class ToolCallStart(val toolName: String) : ParsedToken()
        data class ToolCallEnd(val result: String) : ParsedToken()
        data class SystemLog(val log: String) : ParsedToken()
    }

    /**
     * Transforms a raw string flow into a structured stream of ParsedTokens.
     */
    fun parseStream(rawFlow: Flow<String>): Flow<ParsedToken> {
        return rawFlow.map { chunk ->
            when {
                chunk.startsWith("[TOOL_CALL]") -> {
                    val toolName = chunk.substringAfter("[TOOL_CALL]").trim()
                    ParsedToken.ToolCallStart(toolName)
                }
                chunk.startsWith("[TOOL_END]") -> {
                    val result = chunk.substringAfter("[TOOL_END]").trim()
                    ParsedToken.ToolCallEnd(result)
                }
                chunk.startsWith("[NEXUS_SYSTEM]") -> {
                    ParsedToken.SystemLog(chunk.substringAfter("[NEXUS_SYSTEM]").trim())
                }
                else -> {
                    // Standard markdown text stream token
                    ParsedToken.TextChunk(chunk)
                }
            }
        }
    }
}
