package com.nexusai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.nexusai.app.ui.chat.ChatScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NexusAppEntry()
                }
            }
        }
    }
}

@Composable
fun NexusAppEntry() {
    ChatScreen(
        messages = listOf("Welcome to NexusAI!", "I am ready to assist you today. How can I help?"),
        isStreaming = false,
        onSendMessage = { text -> /* TODO: Implementation pending */ }
    )
}
