package com.nexusai.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Core foundation of the "Agent Mode".
 * Tracks screen context and dynamically reads active UI components to feed the LLM context.
 */
class AgentAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        this.serviceInfo = info
        Log.d("AgentAccessibility", "NexusAI Agent Mode connected to System OS.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Example: Only capture screen output if Agent Mode is visibly active
        // and user requested contextual awareness.
        // val rootNode = rootInActiveWindow
        // parseNodeHierarchy(rootNode)
    }

    /**
     * Recursively reads text representations of the current visible screen.
     */
    private fun parseNodeHierarchy(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return
        if (node.text != null || node.contentDescription != null) {
            val elementContext = node.text ?: node.contentDescription
            Log.d("AgentMode", "Found UI Element: \$elementContext")
            // Stream context to Node.js CLI process
        }
        for (i in 0 until node.childCount) {
            parseNodeHierarchy(node.getChild(i), depth + 1)
        }
    }

    override fun onInterrupt() {
        Log.w("AgentAccessibility", "Agent Mode interrupted by system.")
    }
}
