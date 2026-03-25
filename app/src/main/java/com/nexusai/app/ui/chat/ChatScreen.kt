package com.nexusai.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusai.app.ui.chat.components.AnimatedInputBar

/**
 * Main Chat Screen rendering the streaming conversation with Markdown capability.
 * Mode Switcher allows toggling CLI output visibility for Advanced users.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    // ViewModel state ideally passed here
    messages: List<String> = listOf("Welcome to NexusAI!"),
    isStreaming: Boolean = false,
    onSendMessage: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll logic as streaming continues
    LaunchedEffect(messages.size, isStreaming) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NexusAI Agent") },
                actions = {
                    // Mode Switcher (Simple / Advanced / CLI)
                    TextButton(onClick = { /* Toggle Mode */ }) {
                        Text("Simple Mode")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            AnimatedInputBar(
                isStreaming = isStreaming,
                onSend = onSendMessage,
                onStop = { /* Stop Node Stream */ },
                onVoiceClick = { /* Start STT */ },
                onAttachClick = { /* Open File Picker */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    // Markdown Message Bubble (Placeholder for Markwon wrapper)
                    Card(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.wrapContentWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
