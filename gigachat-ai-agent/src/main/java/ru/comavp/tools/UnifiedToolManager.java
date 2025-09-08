package ru.comavp.tools;

import chat.giga.model.completion.ChatFunction;
import io.modelcontextprotocol.spec.McpSchema;
import ru.comavp.mcp.McpClientRunner;
import ru.comavp.tools.model.ToolResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class UnifiedToolManager {
    private final Map<String, UnifiedToolDefinition> tools;

    public UnifiedToolManager(McpClientRunner mcpClient) {
        this.tools = new HashMap<>();
        loadLocalTools();
        loadMcpTools(mcpClient);
    }

    private void loadLocalTools() {
        for (ToolDefinitions toolDefinition : ToolDefinitions.values()) {
            UnifiedToolDefinition adapter = new LocalToolAdapter(toolDefinition.getToolDefinition());
            tools.put(adapter.getName(), adapter);
        }
    }

    private void loadMcpTools(McpClientRunner mcpClient) {
        McpSchema.ListToolsResult mcpToolsResult = mcpClient.getToolsList();
        for (McpSchema.Tool mcpTool : mcpToolsResult.tools()) {
            UnifiedToolDefinition adapter = new McpToolAdapter(mcpTool, mcpClient::executeTool);
            tools.put(adapter.getName(), adapter);
        }
    }

    public List<ChatFunction> getAllChatFunctions() {
        return tools.values()
                .stream()
                .map(UnifiedToolDefinition::toChatFunction)
                .toList();
    }

    public ToolResult executeTool(String toolName, Map<String, Object> arguments) {
        UnifiedToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return ToolResult.error("Функция не найдена");
        }
        return tool.execute(arguments);
    }

    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
}