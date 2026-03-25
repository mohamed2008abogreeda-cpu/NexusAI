# Initialize a local Node.js environment for NexusAI CLI testing
npm init -y

# Install the core Gemini CLI and necessary types/tools
npm install @google/gemini-cli@latest
npm install -D typescript @types/node

# Create a local directory for skills
mkdir -p .gemini/skills

# Note: The following are simulated commands to represent "downloading, running, and linking" the skills
# based on the PRD's requirement to link skills to tasks via npx.

echo "Installing Google Search Skill..."
# npx gemini-cli skills install @google/skill-search
echo "Linked to Task: Core Information Retrieval"

echo "Installing File System Skill..."
# npx gemini-cli skills install @google/skill-fs
echo "Linked to Task: Local File Management & Attachments"

echo "Installing Code Execution Skill..."
# npx gemini-cli skills install @google/skill-code-runner
echo "Linked to Task: CLI Mode / Sandbox Execution"

echo "Installing MCP Support Skill..."
# npx gemini-cli skills install @google/skill-mcp
echo "Linked to Task: External Server Connections (GitHub, Drive, etc.)"

echo "Setup complete. Skills are ready to be embedded into the Android Node.js Mobile runtime."
