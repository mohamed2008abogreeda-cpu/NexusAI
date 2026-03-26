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
import com.nexusai.app.auth.AuthManager
import com.nexusai.app.auth.KeystoreHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { AuthManager(context, KeystoreHelper()) }
    var showLoginPrompt by remember { mutableStateOf(true) }

    val messages = remember { mutableStateListOf("Welcome to NexusAI!", "I am ready to assist you today. (Node Engine is currently standby)") }
    var isStreaming by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showLoginPrompt) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { androidx.compose.material3.Text("Connect to Google") },
            text = { androidx.compose.material3.Text("Please connect your Google Account to enable NexusAI's Gemini features.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showLoginPrompt = false
                    authManager.startOAuthFlow()
                }) {
                    androidx.compose.material3.Text("Connect Gmail")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLoginPrompt = false }) {
                    androidx.compose.material3.Text("Skip")
                }
            }
        )
    }

    ChatScreen(
        messages = messages,
        isStreaming = isStreaming,
        onSendMessage = { text ->
            if (text.isNotBlank()) {
                messages.add(text)
                isStreaming = true
                coroutineScope.launch {
                    delay(1500)
                    messages.add("Echo: $text\n\n*(Full Gemini engine integration pending)*")
                    isStreaming = false
                }
            }
        }
    )
}
