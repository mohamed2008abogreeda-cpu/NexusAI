const rnBridge = require('rn-bridge');
const { spawn } = require('child_process');

// The active Gemini CLI subprocess
let geminiProcess = null;

// Initialize the bridge connection with Kotlin
rnBridge.channel.on('message', (msg) => {
    try {
        const payload = JSON.parse(msg);
        
        if (payload.action === 'START_CLI') {
            startGeminiCLI();
        } else if (payload.action === 'SEND_INPUT') {
            if (geminiProcess && geminiProcess.stdin) {
                geminiProcess.stdin.write(payload.data + '\n');
            }
        } else if (payload.action === 'STOP_CLI') {
            if (geminiProcess) {
                geminiProcess.kill('SIGINT');
            }
        }
    } catch (e) {
        rnBridge.channel.send(JSON.stringify({ type: 'error', data: 'Failed to process Kotlin IPC message' }));
    }
});

function startGeminiCLI() {
    if (geminiProcess) return;

    // Spawn the loaded gemini-cli module installed in this embedded project
    geminiProcess = spawn('node', ['./node_modules/@google/gemini-cli/bin/gemini.js']);

    // Capture standard output and route it to Kotlin Native
    geminiProcess.stdout.on('data', (data) => {
        const text = data.toString();
        rnBridge.channel.send(JSON.stringify({ type: 'stdout', data: text }));
    });

    // Capture error output
    geminiProcess.stderr.on('data', (data) => {
        const text = data.toString();
        rnBridge.channel.send(JSON.stringify({ type: 'stderr', data: text }));
    });

    geminiProcess.on('close', (code) => {
        rnBridge.channel.send(JSON.stringify({ type: 'exit', data: `CLI exited with code \${code}` }));
        geminiProcess = null;
    });
}

// Signal Kotlin that the Node.js thread is fully booted and ready
rnBridge.channel.send(JSON.stringify({ type: 'system', data: 'NODE_RUNTIME_READY' }));
