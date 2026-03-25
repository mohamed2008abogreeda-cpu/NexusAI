package com.nexusai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nexusai.app.data.entities.Conversation
import com.nexusai.app.data.entities.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    
    // Conversation Queries
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    // Message Queries
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("UPDATE messages SET content = :content, isStreaming = :isStreaming WHERE id = :messageId")
    suspend fun updateMessageContent(messageId: String, content: String, isStreaming: Boolean)

    // Transaction to insert a message and update conversation's updatedAt simultaneously
    @Transaction
    suspend fun addMessageAndUpdateConversation(message: Message, updatedAt: Long) {
        insertMessage(message)
        updateConversationTime(message.conversationId, updatedAt)
    }

    @Query("UPDATE conversations SET updatedAt = :time WHERE id = :id")
    suspend fun updateConversationTime(id: String, time: Long)
}
