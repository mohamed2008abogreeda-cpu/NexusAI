# NexusAI - Detailed Implementation Plan

## Phase 1: Project Initialization & Architecture setup
- [ ] Initialize Android Studio project (Kotlin, Jetpack Compose, Material 3).
- [ ] Configure Gradle with required dependencies (Room, Hilt, Coil, Markwon, CameraX).
- [ ] Set up `nodejs-mobile-android` runtime integration in a native background service.
- [ ] Establish JNI / Unix Domain Socket bridge for IPC between Kotlin and Node.js.
- [ ] Initialize Room Database with SQLCipher for encrypted local storage.

## Phase 2: Authentication & Core Data Layer
- [ ] Implement Google OAuth 2.0 flow using `CustomTabsIntent`.
- [ ] Store OAuth credentials securely in Android Keystore.
- [ ] Create core Room entities (`Conversation`, `Message`, `Skill`, `McpServer`).
- [ ] Implement repositories and data sources with Kotlin Coroutines/Flow.

## Phase 3: Node.js Bridge & Gemini CLI Integration
- [ ] Spawn `npx @google/gemini-cli` subprocess inside the Node.js mobile runtime.
- [ ] Create streaming parser to funnel `stdout`/`stderr` tokens from CLI to Kotlin UI.
- [ ] Handle CLI lifecycle (startup, idle pause, clean shutdown).
- [ ] Implement OTA CLI auto-updater via `npm update` in background `WorkManager`.

## Phase 4: User Interface & Modes (Simple, Advanced, CLI)
- [ ] Build Chat UI using Jetpack Compose (Streaming message list, Markdown parser).
- [ ] Build Multi-line Input Bar with attachment and voice toggles.
- [ ] Implement "Mode Switcher" logic to hide/show terminal output and advanced controls.
- [ ] Build Floating Action Button Overlay (requires `SYSTEM_ALERT_WINDOW`).

## Phase 5: Voice & Media Capabilities
- [ ] Integrate Android `SpeechRecognizer` for STT input.
- [ ] Integrate Android `TextToSpeech` for hands-free output.
- [ ] Implement Storage Access Framework and MediaStore for attaching images/files.
- [ ] Add Image preview and document parsing features before sending to CLI.

## Phase 6: Skills System & MCP Servers
- [ ] Map internal API to Gemini CLI Skills (`tools` definitions).
- [ ] Build `Skills Manager` UI to install, toggle, and view custom `SKILL.md` prompts.
- [ ] Build `MCP Server Manager` UI to connect to external endpoints handling Server-Sent Events/HTTP.
- [ ] Bootstrap default predefined skills via NPM / CLI commands.

## Phase 7: Agent Mode & Automation (The "Agent")
- [ ] Register `AccessibilityService` to allow Agent Mode to read screens and perform taps.
- [ ] Build "Plan & Execute" human-in-the-loop task tracker.
- [ ] Implement Tasker integration (Broadcast Intents/Action Plugins).
- [ ] Build safety sandbox and persistent notification with Emergency Stop.

## Phase 8: Offline Queue & Security
- [ ] Implement `WorkManager` queue for messages sent while offline.
- [ ] Add App Lock (Biometric/PIN) for privacy.
- [ ] Finalize encryption and strict network isolation checks.

## Antigravity AI Skills Allocation
The following internal AI Skills have been loaded into context and assigned to project phases:

1. **`app-builder`**: Assigned to **Phase 1 (Initialization)** to orchestrate project scaffolding and architectural decisions.
2. **`database-design`**: Assigned to **Phase 2 (Data Layer)** to enforce optimal schema design for Room DB and SQLCipher.
3. **`nodejs-best-practices`**: Assigned to **Phase 3 (Node.js Bridge)** to ensure the embedded JS runtime is performant, non-blocking, and cleanly interfaced.
4. **`frontend-aesthetics`**: Assigned to **Phase 4 & 5 (UI/Media)** to guarantee premium, modern Material 3 Jetpack Compose designs with micro-interactions.
5. **`code-reviewer`**: Assigned to **Phase 7 & 8 (Agent/Security)** to rigorously analyze Accessibility Service boundaries and enforce strict security/encryption standards.
6. **`design-spells`**: Assigned to **Phase 4 (UI)** to inject magical micro-interactions and premium UX details into standard Compose components.
7. **`memory-systems`**: Assigned to **Phase 7 (Agent Mode)** to architect short-term, long-term, and temporal knowledge graphs for the autonomous agent's memory.
8. **`bug-hunter`**: Assigned to **All Phases** to systematically isolate and resolve any complex logical or integration errors during the build.
