package com.nexusai.app.repository

import com.nexusai.app.data.dao.AppDao
import com.nexusai.app.data.entities.Conversation
import com.nexusai.app.data.entities.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the application's data. 
 * Orchestrates local Room DB operations.
 */
@Singleton
class NexusRepository @Inject constructor(
    private val appDao: AppDao
) {

    // Streams all conversations robustly using Kotlin Flow
    val allConversations: Flow<List<Conversation>> = appDao.getAllConversations()

    fun getMessagesForConversation(id: String): Flow<List<Message>> {
        return appDao.getMessagesForConversation(id)
    }

    suspend fun startNewConversation(conversation: Conversation) {
        appDao.insertConversation(conversation)
    }

    suspend fun saveMessage(message: Message) {
        appDao.addMessageAndUpdateConversation(message, System.currentTimeMillis())
    }

    suspend fun updateMessageStreaming(messageId: String, content: String, isStreaming: Boolean) {
        appDao.updateMessageContent(messageId, content, isStreaming)
    }

    suspend fun deleteConversation(id: String) {
        appDao.deleteConversation(id)
    }
}
