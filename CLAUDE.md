# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a multi-module Maven project for AI agent implementations that use the Model Context Protocol (MCP). The project contains three modules:

- `java-mcp-server` - MCP server implementation with email functionality
- `deepseek-ai-agent` - AI agent client using DeepSeek API
- `gigachat-ai-agent` - AI agent client using GigaChat API

Both AI agent modules integrate with MCP tools for file operations and can communicate through email via the MCP server.

## Development Commands

### Building the Project
```bash
# Build all modules from root
mvn clean compile

# Build specific module
cd java-mcp-server
mvn clean compile

# Package with dependencies (for MCP server)
cd java-mcp-server
mvn clean package
```

### Running Applications

#### MCP Server (java-mcp-server)
```bash
cd java-mcp-server
# Regular run
mvn exec:java -Dexec.mainClass="ru.comavp.App"

# With logging enabled (local profile)
mvn exec:java -Dexec.mainClass="ru.comavp.App" -Plocal

# Or run the packaged JAR
java -jar target/java-mcp-server-1.0-SNAPSHOT.jar
```

#### DeepSeek AI Agent
```bash
cd deepseek-ai-agent
mvn exec:java -Dexec.mainClass="ru.comavp.App"
```

#### GigaChat AI Agent
```bash
cd gigachat-ai-agent
mvn exec:java -Dexec.mainClass="ru.comavp.App"
```

### Testing
```bash
# Run tests for MCP server
cd java-mcp-server
mvn test

# MCP server has JUnit 5 and Mockito test dependencies
```

## Configuration

All modules use environment variables for configuration via `application.properties`:

- `AUTH_KEY` - API key for AI services (DeepSeek/GigaChat)
- `USERNAME` - Email username for MCP server
- `PASS` - Email password for MCP server  
- `RECIPIENT` - Email recipient for MCP server

## Architecture Overview

### MCP Integration
Both AI agents use `McpClientRunner` to connect to the MCP server, which provides:
- File operations (ReadFileTool, EditFileTool, ListFilesTool)
- Email sending capabilities
- Tool schema generation and validation

### AI Agent Flow
1. Agent starts with configured API client (DeepSeek or GigaChat)
2. Retrieves available MCP tools and converts them to chat functions
3. Handles user input in interactive chat loop
4. Processes tool calls from AI responses
5. Executes tools via MCP and returns results

### Tool System
Custom tool definitions in `tools/` package with:
- `ToolDefinition` interface for tool contracts
- `ToolSchemaUtils` for JSON schema generation
- Function-specific implementations (ReadFileTool, EditFileTool, ListFilesTool)

## Java Version
Project uses Java 19 with Maven compiler source/target set accordingly.

## Dependencies
- Model Context Protocol SDK (0.10.0)
- OpenAI Java client for DeepSeek integration
- GigaChat Java client for GigaChat integration
- Jackson for JSON processing
- Apache Commons for utilities
- Lombok for code generation
- JUnit 5 and Mockito for testing (MCP server only)

## Code Style Guidelines

### File Headers
When creating new Java files, always add an author comment indicating Claude Code as the creator:
```java
/**
 * @author Claude Code
 */
```

**Important**: Only add the `@author` comment when creating new files. When editing existing files, do not add this comment.

## Git Commit Guidelines

### Commit Message Format
- Use single line only with `[claude-code]` prefix
- Be concise and descriptive
- No additional lines, footers, or co-author information
- Examples:
  - `[claude-code] Fix code style issues`
  - `[claude-code] Add unified tool management system`
  - `[claude-code] Implement MCP tool adapter pattern`

## Pull Request Guidelines

### Description Format
- Include only a Summary section
- Use bullet points with dashes
- Focus on concise, focused changes
- No test plan, generated footer, or co-author information
- Example format:
  ```
  ## Summary
  - Add .claude_code_config with commit message guidelines for Claude Code
  - Add CLAUDE.md with comprehensive project structure and development commands documentation
  ```