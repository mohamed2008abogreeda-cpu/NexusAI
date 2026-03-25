# NexusAI — Product Requirements Document

> **Version:** 1.0  
> **Platform:** Android Native  
> **Base:** `npx @google/gemini-cli` — Full Android UI Wrapper  
> **Min SDK:** Android 10 (API 29)  
> **Language:** Kotlin + Jetpack Compose  

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technical Architecture](#2-technical-architecture)
3. [Permissions & System Integration](#3-permissions--system-integration)
4. [Authentication](#4-authentication)
5. [User Modes](#5-user-modes)
6. [Chat Interface](#6-chat-interface)
7. [Skills System](#7-skills-system)
8. [Agent Mode](#8-agent-mode)
9. [Floating Button Overlay](#9-floating-button-overlay)
10. [Tools (Gemini CLI Tools)](#10-tools-gemini-cli-tools)
11. [MCP Server Manager](#11-mcp-server-manager)
12. [File & Media Input](#12-file--media-input)
13. [Voice Features](#13-voice-features)
14. [AI Configuration](#14-ai-configuration)
15. [Conversation Management](#15-conversation-management)
16. [Checkpoints](#16-checkpoints)
17. [Offline Mode](#17-offline-mode)
18. [Export & Share](#18-export--share)
19. [Tasker / Automation Integration](#19-tasker--automation-integration)
20. [Security & Privacy](#20-security--privacy)
21. [Localization](#21-localization)
22. [UI/UX Specifications](#22-uiux-specifications)
23. [Screen Map](#23-screen-map)
24. [Data Models](#24-data-models)
25. [Technical Constraints & Performance](#25-technical-constraints--performance)

---

## 1. Project Overview

### 1.1 Core Concept

NexusAI is a **full Android native wrapper** for `npx @google/gemini-cli`. It embeds a real Node.js runtime inside the app and runs the CLI directly — every feature, every tool, every update in the CLI is automatically available in the app. The user authenticates via their Google account (same as `gemini auth login`) with no API key required.

### 1.2 What It Is NOT

- It is **not** a thin API client calling Gemini REST endpoints
- It is **not** rebuilding Gemini CLI from scratch
- It **is** a full UI shell running the actual CLI binary inside the device

### 1.3 Core Value Proposition

| For | Value |
|-----|-------|
| Regular Users | Full Gemini AI power without needing Terminal |
| Power Users | MCP servers, Skills, custom prompts, all CLI tools |
| Developers | CLI Mode, Shell access, Agent Mode, Tasker integration |

---

## 2. Technical Architecture

### 2.1 Stack

```
┌─────────────────────────────────────────┐
│           Android UI Layer              │
│     Kotlin + Jetpack Compose            │
│     Material Design 3 (Material You)    │
├─────────────────────────────────────────┤
│           CLI Bridge Layer              │
│     JNI / IPC / Unix Socket             │
├─────────────────────────────────────────┤
│         Node.js Runtime Layer           │
│     nodejs-mobile-android               │
│     runs: npx @google/gemini-cli        │
├─────────────────────────────────────────┤
│           Data Layer                    │
│     Room DB (SQLCipher) + DataStore     │
│     Android Keystore (tokens/keys)      │
└─────────────────────────────────────────┘
```

### 2.2 Tech Stack Details

| Layer | Technology |
|-------|-----------|
| UI | Kotlin, Jetpack Compose, Material3 |
| Node.js Runtime | `nodejs-mobile-android` library |
| CLI Execution | `npx @google/gemini-cli` via Node.js |
| Bridge | JNI + Unix Domain Socket (IPC) |
| Database | Room + SQLCipher (encrypted) |
| Auth | Google OAuth 2.0 via Custom Chrome Tab |
| Token Storage | Android Keystore |
| File Access | Storage Access Framework + MediaStore |
| Cloud | Google Drive API v3 |
| Voice Input | Android SpeechRecognizer API |
| Voice Output | Android TextToSpeech API |
| Background Work | WorkManager |
| Encryption | AES-256 + Android Keystore |
| Offline Queue | WorkManager + Room |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Image Loading | Coil |
| Markdown | Markwon library |

### 2.3 Data Flow

```
User Input (text/voice/file)
    ↓
Android UI → Input Processor
    ↓
CLI Bridge (IPC Socket)
    ↓
Node.js Runtime → npx @google/gemini-cli
    ↓
Gemini API (via Google Account OAuth)
    ↓
Streaming Response → Bridge → UI (token by token)
    ↓
Room DB (save message) + Render in Compose
```

### 2.4 Node.js Runtime Notes

- Node.js runs in a **separate native thread** (never on UI thread)
- Runtime starts on app launch, stays alive in background service
- CLI process spawned per conversation session
- Stdin/Stdout/Stderr piped via Unix Socket to Kotlin layer
- Runtime shuts down gracefully on app background after N minutes (configurable)
- CLI auto-update: app checks for new `@google/gemini-cli` version on each launch and updates via `npm update` in background

---

## 3. Permissions & System Integration

### 3.1 Required Permissions (Manifest)

```xml
<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29"/>
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- System -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<!-- Overlay -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Accessibility -->
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />

<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />

<!-- Device -->
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Usage Stats -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions"/>

<!-- Battery -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

### 3.2 Special Permissions Flow

Each special permission is requested contextually (not all at once at startup):

| Permission | When Requested | Purpose |
|-----------|---------------|---------|
| `SYSTEM_ALERT_WINDOW` | First use of Floating Button | Draw overlay above apps |
| `BIND_ACCESSIBILITY_SERVICE` | First use of Agent Mode screen actions | Read/interact with screen |
| `MANAGE_EXTERNAL_STORAGE` | First file write operation | Full file system access |
| `BIND_NOTIFICATION_LISTENER` | User enables notification reading in settings | AI reads notifications |
| `PACKAGE_USAGE_STATS` | Agent Mode app control feature | Know which apps are running |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Agent Mode enabled | Keep agent running |
| `RECEIVE_BOOT_COMPLETED` | Auto-start enabled in settings | Start on boot |

### 3.3 Accessibility Service

NexusAI registers an `AccessibilityService` enabling:
- Reading text content from any app on screen
- Simulating taps, swipes, text input in any app
- Used exclusively by Agent Mode for task automation
- User must explicitly enable in Android Accessibility Settings
- Clear disclosure shown before requesting

### 3.4 Device Admin (Optional)

Optional Device Admin registration for:
- Remote wipe of NexusAI data
- Enforce app lock policy

---

## 4. Authentication

### 4.1 Google OAuth Flow

Mirrors `gemini auth login` behavior:

1. App opens Google Sign-In via `CustomTabsIntent`
2. OAuth scopes: `openid`, `email`, `profile`, `https://www.googleapis.com/auth/generative-language`
3. Tokens stored in Android Keystore (encrypted)
4. Refresh token auto-renewed silently in background
5. On token expiry: silent refresh → fallback to re-login if refresh fails

### 4.2 Multi-Account Support

- Multiple Google accounts supported
- Account switcher in top bar / settings
- Each account has independent: conversation history, settings, quota display
- Active account shown with avatar in sidebar

### 4.3 Quota Display

- Show current Gemini usage vs quota (from API response headers)
- Warning when approaching limit
- Link to upgrade plan

---

## 5. User Modes

Three modes switchable at any time via bottom sheet or settings:

### 5.1 Simple Mode
- **Target:** Non-technical users
- **UI:** Clean chat interface (WhatsApp/ChatGPT style)
- **Available:** Chat, image/file upload, voice, Google Search tool
- **Hidden:** CLI output, shell, MCP, advanced settings

### 5.2 Advanced Mode
- **Target:** Power users
- **UI:** Chat + collapsible sidebar with tools panel
- **Available:** Everything in Simple + MCP Servers, all Tools, Skills, AI config, System Prompt editor
- **Hidden:** Raw CLI output, shell commands

### 5.3 CLI Mode
- **Target:** Developers / Experts
- **UI:** Split view — Chat on top, raw CLI stdout/stderr on bottom (resizable)
- **Available:** Everything + Shell execution, raw output, debug logs, Node.js REPL
- **Extra:** Command palette for direct CLI commands

---

## 6. Chat Interface

### 6.1 Message List

- Streaming render: tokens appear word-by-word as they arrive from CLI stdout
- Full Markdown rendering (Markwon): headings, bold, italic, tables, code blocks, lists, links
- Syntax highlighting in code blocks with language label + copy button
- Images rendered inline
- File attachments shown as cards with name/size/type icon
- Long-press any message → context menu: Copy, Share, Delete, Regenerate from here, Edit (user messages only)
- Pull-to-refresh to reload conversation from DB

### 6.2 Input Bar

```
[ 📎 Attach ] [ 🎙️ Voice ] [ _________________________ text field _________________________ ] [ ➤ Send / ⏹ Stop ]
```

- Text field: multiline, auto-expand up to 6 lines, then scrollable
- Send button becomes Stop button while streaming
- Attach opens bottom sheet: Camera / Gallery / Files / Drive / Other Apps
- Voice button: hold to record, release to send (or tap for toggle mode)
- Slash commands: type `/` to open command palette (Skills, Tools, Modes)
- `@` mention: type `@` to reference a Skill or MCP tool

### 6.3 Message Actions

- Tap avatar/icon → copy message
- Long press → full context menu
- Swipe right on message → reply/quote
- Regenerate button appears below last assistant message
- Edit + resend for user messages

---

## 7. Skills System

Skills are modular capability packages that extend what Gemini CLI can do. They map directly to Gemini CLI's skills/tools system.

### 7.1 Skills Architecture

```
Skill Package Structure:
├── skill.json          ← metadata (name, description, version, author)
├── SKILL.md            ← instructions injected into system prompt
├── tools/              ← custom tool definitions (JSON Schema)
│   └── tool_name.json
└── assets/             ← optional supporting files
```

### 7.2 Skills Sources

#### A — Google Official Skills Library
- Built-in marketplace browsing screen
- Curated Skills from Google (coding assistant, writing helper, data analyst, etc.)
- One-tap install
- Auto-update when CLI updates

#### B — Custom Skills Builder
- In-app Skills editor
- Fields: Name, Description, SKILL.md content (system prompt additions), Tool definitions (JSON)
- Live preview: test Skill in sandbox conversation before saving
- Skill versioning

#### C — Skills Marketplace (Community)
- Users can publish their Skills to shared marketplace
- Browse, search, rate, download community Skills
- Report / flag inappropriate Skills
- Skills sandboxed — no arbitrary code execution outside CLI tools scope

#### D — Import from File
- Import `.zip` Skill package from device storage or URL
- Import from GitHub repo URL (fetches skill.json + SKILL.md)
- Validate package before import

#### E — AI Auto-Download (Smart Skills)
- When user asks something requiring a Skill not installed, AI detects the need
- Suggests: *"This task works better with the [Data Analyst Skill] — install it? (1 tap)"*
- After user approval, AI downloads + activates Skill automatically and continues the task
- Can auto-install from official library without asking if user enables "Auto Skill Install" in settings

### 7.3 Skills Manager Screen

- List of installed Skills with toggle (active/inactive per conversation or globally)
- Active Skills shown as chips in chat header
- Install / Update / Delete / Export
- Skills search
- Skill details page: description, tools it adds, SKILL.md preview, author, version, ratings

### 7.4 Skills Activation

- Global activation: Skill always active across all conversations
- Per-conversation: activate Skill only in specific conversation
- Auto-suggest: AI recommends relevant Skills based on conversation context
- Max active Skills per conversation: configurable (default: 10)

---

## 8. Agent Mode

Agent Mode lets NexusAI autonomously complete multi-step tasks on the device without continuous user input.

### 8.1 What Agent Mode Can Do

- Execute multi-step plans involving: file operations, app interactions, web browsing, API calls
- Use Accessibility Service to interact with any app on screen
- Read screen content from any app
- Send messages in apps (WhatsApp, Telegram, Gmail, etc.) on user command
- Fill forms in apps
- Take screenshots and analyze them
- Create, move, rename, delete files
- Download files from the web
- Run sequences of Gemini CLI tool calls autonomously
- Monitor conditions and trigger actions (e.g., "when battery < 20%, send me a notification")

### 8.2 Agent Task Flow

```
User defines task (natural language)
    ↓
NexusAI breaks task into steps (using Gemini thinking)
    ↓
Shows plan to user for approval
    ↓
User approves → Agent executes step by step in background
    ↓
Progress shown in persistent notification + in-app progress card
    ↓
Each step result logged in Agent Log screen
    ↓
On completion: summary notification + full log available
    ↓
On error: pause + notify user + ask how to proceed
```

### 8.3 Agent Safety Controls

- **Human-in-the-loop by default:** user approves plan before execution
- **Sensitive action confirmation:** actions like sending messages, deleting files, purchasing — always require explicit tap confirmation
- **Emergency Stop:** persistent notification with "STOP AGENT" button always visible during execution
- **Sandbox mode:** dry-run that shows what agent WOULD do without doing it
- **Action log:** every action logged with timestamp, reversible where possible
- **Scope limits:** user can restrict agent to specific apps only

### 8.4 Agent Background Service

- Runs as Foreground Service with persistent notification
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` used to prevent killing
- Wakelock held during critical operation sequences
- Can run while screen is off

---

## 9. Floating Button Overlay

A floating action button that appears above all apps, giving instant access to NexusAI.

### 9.1 Behavior

- Small floating bubble (like Facebook Chat Heads or Samsung Edge Panel)
- Appears above all apps when NexusAI is running in background
- Draggable — user positions it anywhere on screen
- Auto-snaps to nearest screen edge when released
- Can be hidden/shown via Quick Settings Tile or notification

### 9.2 Floating Button Actions

**Single tap:**
- Opens mini chat overlay (compact chat window above current app)
- Or opens full NexusAI app (user-configurable)

**Long press:**
- Opens radial quick-action menu:
  - 🎙️ Voice input directly
  - 📸 Screenshot + analyze current screen
  - 📋 Analyze clipboard content
  - ⚡ Quick Skills shortcuts (user-defined)
  - ✖️ Dismiss button

**Double tap:**
- Activate voice input immediately (hands-free)

### 9.3 Mini Overlay Chat

When opened via floating button:
- Compact chat window (covers ~50% of screen, semi-transparent background)
- Full chat functionality in compact form
- Swipe down to minimize back to bubble
- Tap expand icon to open full app
- Context-aware: can read text from app behind it (with permission)

### 9.4 Screen Context Reading

When floating button is active + Accessibility permission granted:
- Can extract text from current screen automatically
- User can say "analyze what's on my screen" and NexusAI reads screen content and passes it to Gemini
- "Help me reply to this message" → reads message on screen → generates reply

---

## 10. Tools (Gemini CLI Tools)

All tools available in `npx @google/gemini-cli` are exposed in the app.

### 10.1 Tool Availability by Mode

| Tool | Simple | Advanced | CLI |
|------|--------|----------|-----|
| Google Search | ✅ | ✅ | ✅ |
| Image Analysis | ✅ | ✅ | ✅ |
| File Read | ❌ | ✅ | ✅ |
| File Write | ❌ | ✅ | ✅ |
| Web Fetch / Browse | ❌ | ✅ | ✅ |
| Code Execution | ❌ | ✅ | ✅ |
| Shell Commands | ❌ | ❌ | ✅ |
| MCP Tools | ❌ | ✅ | ✅ |
| Custom Skills Tools | ❌ | ✅ | ✅ |

### 10.2 Tool Call Visualization

- Each tool call shown as expandable card in chat:
  ```
  🔧 Tool: google_search
  Query: "latest Android release"
  ▼ Show results
  ```
- In CLI Mode: raw tool call JSON shown in bottom panel
- Tool execution status: pending → running → done/error
- Tool results expandable/collapsible

---

## 11. MCP Server Manager

### 11.1 MCP Manager Screen

- List of configured MCP Servers with connection status indicator (🟢/🔴)
- Per-server: Name, URL, Transport type (SSE/HTTP/Stdio), active tools count
- Add / Edit / Delete / Duplicate servers
- Test connection button (pings server, shows latency)
- Enable/Disable server without deleting
- Drag to reorder priority

### 11.2 Adding MCP Server

Fields:
- `Name` (display name)
- `URL` (server endpoint)
- `Transport` (SSE / HTTP / Stdio)
- `Auth Type` (None / API Key / Bearer Token / OAuth)
- `Auth Value` (stored encrypted in Keystore)
- `Environment Variables` (key-value pairs)
- `Auto-connect on startup` toggle

### 11.3 Presets

One-tap setup for common servers:
- GitHub (`github.mcp.run`)
- Google Drive
- Jira / Confluence
- Notion
- PostgreSQL / SQLite
- Slack
- Linear

### 11.4 Tools Discovery

After connecting: automatically fetch and display available tools from the server. Show tool name, description, input schema. User can disable specific tools from a server.

---

## 12. File & Media Input

### 12.1 Input Sources

| Source | Implementation |
|--------|---------------|
| Camera (photo) | `CameraX` → capture → attach |
| Camera (video) | `CameraX` → record → attach |
| Gallery | `PhotoPicker` API (Android 13+) / `MediaStore` |
| File Manager | `Storage Access Framework` → `DocumentPicker` |
| Google Drive | `Google Drive Picker API` |
| Share Intent | `ACTION_SEND` / `ACTION_SEND_MULTIPLE` receiver |
| Clipboard | Paste image or file path from clipboard |
| URL | User pastes URL → app fetches content |
| Other Cloud | Deep link to Dropbox/OneDrive pickers |

### 12.2 Supported File Types

- **Images:** JPG, PNG, WebP, GIF, HEIC, BMP, SVG
- **Video:** MP4, MOV, AVI, MKV, WebM
- **Documents:** PDF, DOCX, DOC, TXT, MD, RTF
- **Code:** All source code extensions (.py, .js, .kt, .java, .cpp, .ts, etc.)
- **Data:** CSV, JSON, XML, YAML, TOML
- **Audio:** MP3, WAV, OGG, M4A
- **Archives:** ZIP (auto-extract and attach contents)

### 12.3 File Handling

- Files compressed/optimized before upload if over size threshold (configurable)
- Multi-file attach in single message
- File preview before sending
- Files stored temporarily in app cache, cleared after session
- Option to save AI-generated files to device storage or Drive

---

## 13. Voice Features

### 13.1 Voice Input (Speech-to-Text)

- **Engine:** Android `SpeechRecognizer` (on-device, no extra API needed)
- Real-time transcription shown in input field while speaking
- Tap mic → start, tap again → stop and send
- Hold mic → push-to-talk mode
- Auto-send after silence detection (optional, configurable silence timeout)
- Language auto-detected or manually selected
- Arabic, English, French + all Android-supported languages

### 13.2 Voice Output (Text-to-Speech)

- **Engine:** Android `TextToSpeech` API + support for 3rd party TTS engines
- Play button on every assistant message
- Global settings: voice, speed (0.5x–2.0x), pitch, language
- Continuous play mode: every new assistant message read aloud automatically
- Pause/Resume/Skip controls in persistent notification when playing
- Arabic voice supported (Google TTS Arabic)

### 13.3 Hands-Free Mode

Combinning input + output:
- Enable: floating mic always listening (wake-word or button)
- Speak → AI responds → reads response aloud → listens again
- Works with floating button overlay
- Background service keeps mic active (foreground service + notification)

---

## 14. AI Configuration

### 14.1 GEMINI.md — System Prompt Editor

- Full-screen markdown editor
- Save multiple named system prompt profiles
- Switch profiles per-conversation or globally
- Import: paste text, load from `.md` file, load from Drive
- Export: save as `.md` file, share
- Character counter
- Live preview of formatted markdown

### 14.2 Model Settings

| Setting | Type | Range / Options | Default |
|---------|------|-----------------|---------|
| `model` | Dropdown | gemini-2.5-pro, gemini-2.0-flash, gemini-1.5-pro, gemini-1.5-flash | gemini-2.0-flash |
| `temperature` | Slider | 0.0 – 2.0, step 0.1 | 1.0 |
| `maxOutputTokens` | Input | 1,000 – 128,000 | 8,192 |
| `topP` | Slider | 0.0 – 1.0, step 0.01 | 0.95 |
| `topK` | Slider | 1 – 100 | 64 |
| `thinkingBudget` | Slider | 0 – 32,768 | 8,192 |
| `safetySettings` | Per-category dropdown | OFF / LOW / MEDIUM / HIGH | MEDIUM |

### 14.3 Tool Settings (per conversation)

- Toggle each tool on/off
- Google Search: on/off
- Code execution: on/off
- Shell access: on/off (CLI Mode only)
- MCP Servers: multi-select which servers are active

---

## 15. Conversation Management

### 15.1 Conversation List Screen

- All conversations sorted by last updated (default)
- Sort options: date, name, pinned first
- Search: full-text search across titles and message content (Room FTS)
- Filter by: tags, date range, model used, has attachments
- Swipe left on conversation → delete
- Swipe right → pin/unpin
- Long press → multi-select → bulk delete / export / tag

### 15.2 Conversation Actions

- Rename conversation (auto-named by AI from first message)
- Pin / unpin
- Tag with custom labels (color-coded)
- Organize in folders
- Duplicate conversation (fork)
- Share conversation (export)
- Delete with confirmation

### 15.3 In-Conversation Navigation

- Jump to top / bottom buttons appear when scrolled
- Message search within conversation
- Message count shown
- Timestamps on each message (relative + absolute on tap)

---

## 16. Checkpoints

### 16.1 Auto-Save Triggers

- Every N messages (default: 5, configurable in settings)
- On app background / minimize
- On internet disconnect
- On conversation switch
- On app crash (try/catch with emergency save)

### 16.2 Checkpoint Data

Each checkpoint stores:
- All messages (user + assistant) with timestamps
- Active model + all model settings at time of checkpoint
- Active system prompt content
- Active MCP servers list
- Active Skills list
- Attached files metadata (not file content)

### 16.3 Checkpoint Restore

- Conversation detail screen shows checkpoint history
- Tap any checkpoint → preview → restore
- Restore creates a fork (original is preserved)

---

## 17. Offline Mode

### 17.1 Offline Queue

- User can write messages while offline
- Messages stored in `pending_messages` Room table
- UI shows "Queued" badge on pending messages
- `WorkManager` constraint: `NETWORK_REQUIRED` — sends when connection restores
- Messages sent in order (FIFO)
- Failed messages retry with exponential backoff (max 3 retries)
- User can cancel any queued message

### 17.2 Offline Access

- All saved conversations readable offline (stored in Room DB)
- Conversation search works offline
- Settings and Skills browsable offline
- Skills editing works offline

### 17.3 Network Status

- Persistent connectivity banner when offline ("No connection — messages will be sent when online")
- Disappears automatically when reconnected
- Shows queue count: "3 messages waiting to send"

---

## 18. Export & Share

### 18.1 Export Formats

| Format | Content |
|--------|---------|
| PDF | Full conversation, styled, with timestamps |
| Markdown (.md) | Clean markdown, ready for Obsidian/Notion |
| Plain Text (.txt) | No formatting |
| HTML | Self-contained webpage |
| JSON | Full data export including metadata |

### 18.2 Export Options

- Full conversation or selected range (from message X to message Y)
- Include/exclude: timestamps, model info, tool calls, file attachments
- Include code blocks as-is or with syntax highlighting (HTML only)

### 18.3 Share Targets

- Android Share Sheet → any installed app
- Save to device storage (user picks folder)
- Save to Google Drive (direct upload)
- Copy to clipboard (text/markdown)

---

## 19. Tasker / Automation Integration

### 19.1 Tasker Plugin

NexusAI registers as a **Tasker Action Plugin** and **Event Plugin**.

#### Actions (NexusAI does something):
- `SendMessage(conversation_id?, message, skill?)` — send a message, get response
- `StartAgentTask(task_description)` — launch Agent Mode task
- `ActivateSkill(skill_name)` — activate a Skill
- `SwitchMode(simple|advanced|cli)` — change user mode
- `ExportConversation(conversation_id, format, destination)` — export

#### Events (NexusAI triggers Tasker):
- `OnResponseReceived(conversation_id, message_text)` — AI responded
- `OnAgentTaskCompleted(task_id, success, summary)` — agent done
- `OnAgentTaskError(task_id, error)` — agent failed

### 19.2 Intent API (for other automation apps)

Broadcast Intents for integration with Automate, MacroDroid, Bixby Routines, etc.:

```
# Send message
Intent Action: ai.nexus.ACTION_SEND_MESSAGE
Extras:
  message: String
  conversation_id: String (optional, creates new if absent)
  skill: String (optional)
  response_intent: String (optional broadcast action for response)

# Response broadcast
Intent Action: ai.nexus.RESPONSE_RECEIVED
Extras:
  conversation_id: String
  response_text: String
  success: Boolean
```

### 19.3 Shortcuts API

- Android App Shortcuts (long-press icon) — configurable shortcuts to specific conversations or Skills
- Android Quick Settings Tile — toggle Floating Button on/off, start new chat
- Android Widget — (v2.0) mini chat widget on home screen

---

## 20. Security & Privacy

### 20.1 Data Encryption

- Database: Room + SQLCipher with AES-256
- OAuth Tokens: Android Keystore
- MCP Server credentials: Android Keystore
- App Lock PIN: hashed with bcrypt, stored in EncryptedSharedPreferences
- All files in app cache: encrypted at rest

### 20.2 App Lock (Optional)

User-configurable in Settings > Privacy:

- **Biometric:** Fingerprint / Face ID via BiometricPrompt API
- **PIN:** 4–8 digit PIN, set by user
- **Lock trigger:** On app background / after N minutes idle (user sets)
- **Recent Apps protection:** Blur app thumbnail in recents when locked

### 20.3 Data Isolation

- No analytics SDK
- No crash reporting SDK that sends data without consent
- No third-party ads
- Network traffic: only to Gemini API endpoints + configured MCP servers
- Conversation data: never leaves device except to Gemini API

### 20.4 Data Management

- `Settings > Privacy > Delete All Data` — wipes DB, cache, keystore entries
- Per-conversation delete
- Auto-delete conversations older than N days (optional, off by default)

---

## 21. Localization

### 21.1 Supported Languages (v1.0)

Arabic (ar), English (en), French (fr), German (de), Spanish (es), Turkish (tr), Hindi (hi), Portuguese (pt), Japanese (ja), Chinese Simplified (zh-CN)

### 21.2 RTL Support

- Full RTL layout mirroring for Arabic and other RTL languages
- Compose `CompositionLocalProvider` with `LocalLayoutDirection`
- All animations, swipe directions, icon placements mirror correctly
- Arabic numerals option in settings

### 21.3 Auto-Detection

- App language follows system language by default
- Override available in Settings > Language
- AI responses in same language as user input (Gemini handles this naturally)

---

## 22. UI/UX Specifications

### 22.1 Design System

- **Framework:** Material Design 3 (Material You)
- **Dynamic Color:** App colors adapt to Android 12+ wallpaper colors
- **Theme:** System auto (follows device Dark/Light mode) with manual override
- **Typography:** Google Sans / Roboto

### 22.2 Color Tokens (Material3)

```
Primary: MaterialTheme.colorScheme.primary
OnPrimary: MaterialTheme.colorScheme.onPrimary
Surface: MaterialTheme.colorScheme.surface
SurfaceVariant: MaterialTheme.colorScheme.surfaceVariant
Background: MaterialTheme.colorScheme.background
```

### 22.3 Animations

- Message appear: fade-in + slide-up (150ms)
- Streaming text: character-level render (no animation, just append)
- Screen transitions: Material shared element transitions
- Floating button: spring physics on drag + snap
- Tool call card expand/collapse: AnimatedVisibility (200ms)
- All animations respect `ANIMATOR_DURATION_SCALE = 0` accessibility setting

### 22.4 Responsive Layout

- Phones: single-column
- Tablets (sw600dp): two-column (conversation list + chat)
- Foldables: adaptive layout on fold/unfold

---

## 23. Screen Map

```
App Start
└── Onboarding (first launch only)
    └── Google Sign-In
        └── Main Screen

Main Screen
├── Conversation List (home)
│   ├── Search
│   ├── Folder / Tag Filter
│   └── New Conversation
│
├── Chat Screen
│   ├── Message List (streaming)
│   ├── Input Bar (text/voice/attach)
│   ├── Tool Call Cards
│   ├── Skills Chips (active)
│   ├── Mode Switcher
│   └── Conversation Settings (overflow)
│
├── Skills Manager
│   ├── Installed Skills
│   ├── Skills Marketplace (browse/search)
│   ├── Custom Skill Builder
│   └── Import Skill
│
├── MCP Server Manager
│   ├── Server List
│   ├── Add/Edit Server
│   └── Server Tool List
│
├── Agent Mode
│   ├── Task Input
│   ├── Plan Review
│   ├── Execution Progress
│   └── Agent Log
│
├── AI Settings
│   ├── System Prompt Editor
│   ├── Model Config (sliders)
│   └── Tools Toggle
│
└── App Settings
    ├── Account (Google accounts)
    ├── Mode (Simple/Advanced/CLI)
    ├── Privacy (App Lock, Delete Data)
    ├── Floating Button config
    ├── Voice settings
    ├── Notifications
    ├── Language
    ├── Automation (Tasker/Intents)
    ├── Storage & Cache
    └── About / Update CLI
```

---

## 24. Data Models

### 24.1 Core Entities (Room)

```kotlin
@Entity
data class Conversation(
    @PrimaryKey val id: String,           // UUID
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val modelId: String,
    val systemPromptId: String?,
    val mode: UserMode,                   // SIMPLE, ADVANCED, CLI
    val accountId: String,
    val isPinned: Boolean,
    val tags: List<String>,               // JSON array
    val folderId: String?
)

@Entity
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: MessageRole,                // USER, ASSISTANT, TOOL, SYSTEM
    val content: String,
    val timestamp: Long,
    val toolCallsJson: String?,           // Serialized tool calls
    val attachmentsJson: String?,         // Serialized file metadata
    val isStreaming: Boolean,
    val tokenCount: Int?
)

@Entity
data class Checkpoint(
    @PrimaryKey val id: String,
    val conversationId: String,
    val timestamp: Long,
    val messagesJson: String,             // Serialized message list
    val settingsSnapshotJson: String      // Model settings at time of checkpoint
)

@Entity
data class McpServer(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val transport: McpTransport,          // SSE, HTTP, STDIO
    val authType: McpAuthType,
    val authKeyAlias: String?,            // Key in Keystore
    val isEnabled: Boolean,
    val toolsJson: String?                // Cached tool list
)

@Entity
data class Skill(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val source: SkillSource,             // GOOGLE, COMMUNITY, CUSTOM, IMPORTED
    val skillMdContent: String,
    val toolsJson: String?,
    val isEnabled: Boolean,
    val isGlobal: Boolean
)

@Entity
data class PendingMessage(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val attachmentsJson: String?,
    val createdAt: Long,
    val retryCount: Int
)

@Entity
data class SystemPromptProfile(
    @PrimaryKey val id: String,
    val name: String,
    val content: String,
    val createdAt: Long,
    val isDefault: Boolean
)
```

---

## 25. Technical Constraints & Performance

### 25.1 Performance Targets

| Metric | Target |
|--------|--------|
| Cold start time | < 3 seconds |
| UI frame rate | 60 FPS sustained |
| First token latency | < 500ms after send |
| Node.js startup time | < 2 seconds (background pre-warm) |
| DB query (conversation list) | < 50ms |
| Memory usage (idle) | < 200 MB RAM |
| Memory usage (active chat) | < 400 MB RAM |
| APK size | < 80 MB |

### 25.2 Threading Model

- **Main Thread:** Compose UI only
- **IO Dispatcher:** All DB operations, file I/O
- **Default Dispatcher:** Business logic, JSON parsing
- **Node.js Thread:** Dedicated native thread, never touches UI
- **Agent Service Thread:** Foreground service thread for agent tasks

### 25.3 CLI Update Strategy

```
On app launch:
1. Check current version: `npm view @google/gemini-cli version`
2. Compare with installed version
3. If newer → download in background (WorkManager, WiFi preferred)
4. Hot-swap: new version ready for next CLI session
5. Notify user if major version update
```

### 25.4 Node.js Runtime Lifecycle

```
App Launch → Pre-warm Node.js (background)
First Message → Start CLI session
Active Session → Runtime stays alive
App Background > 10min → Pause runtime (configurable)
App Killed → Runtime stops, state saved to DB
App Reopen → Resume from last Checkpoint
```

### 25.5 Build Configuration

```gradle
minSdk = 29          // Android 10
targetSdk = 35       // Android 15
compileSdk = 35

abiFilters = ["arm64-v8a", "armeabi-v7a", "x86_64"]
// x86 for emulator support during development

buildTypes {
    release {
        minifyEnabled = true
        proguardFiles = getDefaultProguardFile("proguard-android-optimize.txt")
    }
}

// Node.js native library included via AAR
implementation("io.github.nodejs-mobile:nodejs-mobile-android:0.3.0")
```

---

*NexusAI PRD v1.0 — Ready for implementation*
