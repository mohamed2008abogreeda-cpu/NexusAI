package com.nexusai.app.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

/**
 * A highly polished, animated multi-line input bar.
 * Integrates "Design Spells" micro-interactions (e.g. spring Send button pop-in).
 */
@Composable
fun AnimatedInputBar(
    isStreaming: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onVoiceClick: () -> Unit,
    onAttachClick: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val canSend = text.isNotBlank() && !isStreaming

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Attachment Button
            IconButton(onClick = onAttachClick) {
                Icon(
                    imageVector = Icons.Rounded.AddCircle,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Input Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        "Talk to NexusAI...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = 6
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Animated Send / Voice / Stop Button Transition
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isStreaming,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 })
                ) {
                    FilledIconButton(onClick = onStop, shape = RoundedCornerShape(14.dp)) {
                        // Pretend there is a Stop icon here
                        Box(modifier = Modifier.size(14.dp).background(MaterialTheme.colorScheme.onPrimary))
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isStreaming && !canSend,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = onVoiceClick) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isStreaming && canSend,
                    enter = fadeIn() + slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialOffsetY = { 40 }),
                    exit = fadeOut()
                ) {
                    FilledIconButton(
                        onClick = {
                            onSend(text)
                            text = ""
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
