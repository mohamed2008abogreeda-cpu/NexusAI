package com.nexusai.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

enum class UserMode { SIMPLE, ADVANCED, CLI }
enum class MessageRole { USER, ASSISTANT, TOOL, SYSTEM }

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val modelId: String,
    val systemPromptId: String? = null,
    val mode: String = UserMode.SIMPLE.name,
    val accountId: String,
    val isPinned: Boolean = false,
    val tagsJson: String = "[]",
    val folderId: String? = null
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // String representation of MessageRole
    val content: String,
    val timestamp: Long,
    val toolCallsJson: String? = null,
    val attachmentsJson: String? = null,
    val isStreaming: Boolean = false,
    val tokenCount: Int? = null
)

@Entity(tableName = "mcp_servers")
data class McpServer(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val transport: String,
    val authType: String,
    val authKeyAlias: String? = null,
    val isEnabled: Boolean = true
)

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val version: String,
    val skillMdContent: String,
    val isEnabled: Boolean = true
)
