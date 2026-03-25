package com.nexusai.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Dummy entities for initial compilation
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Conversation(@PrimaryKey val id: String, val title: String)

@Entity
data class Message(@PrimaryKey val id: String, val content: String)

@Database(entities = [Conversation::class, Message::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    // Setup DAOs here
    // abstract fun conversationDao(): ConversationDao

    companion object {
        const val DATABASE_NAME = "nexusai_encrypted.db"
    }
}
