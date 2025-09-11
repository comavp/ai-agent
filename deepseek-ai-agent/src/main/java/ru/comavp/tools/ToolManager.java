package ru.comavp.tools;

import com.openai.models.chat.completions.ChatCompletionTool;
import io.modelcontextprotocol.spec.McpSchema;
import ru.comavp.mcp.McpClientRunner;
import ru.comavp.tools.adapters.LocalToolAdapter;
import ru.comavp.tools.adapters.McpToolAdapter;
import ru.comavp.tools.adapters.ToolAdapter;
import ru.comavp.tools.model.ToolResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Claude Code
 */
public class ToolManager {

    private final Map<String, ToolAdapter> tools;

    public ToolManager(McpClientRunner mcpClient) {
        this.tools = new HashMap<>();
        loadLocalTools();
        loadMcpTools(mcpClient);
    }

    private void loadLocalTools() {
        for (ToolDefinitions toolDefinition : ToolDefinitions.values()) {
            ToolAdapter adapter = new LocalToolAdapter(toolDefinition.getToolDefinition());
            tools.put(adapter.getName(), adapter);
        }
    }

    private void loadMcpTools(McpClientRunner mcpClient) {
        McpSchema.ListToolsResult mcpToolsResult = mcpClient.getToolsList();
        for (McpSchema.Tool mcpTool : mcpToolsResult.tools()) {
            ToolAdapter adapter = new McpToolAdapter(mcpTool, mcpClient::executeTool);
            tools.put(adapter.getName(), adapter);
        }
    }

    public List<ChatCompletionTool> getAllChatCompletionTools() {
        return tools.values()
                .stream()
                .map(ToolAdapter::toChatCompletionTool)
                .toList();
    }

    public ToolResult executeTool(String toolName, Map<String, Object> arguments) {
        ToolAdapter tool = tools.get(toolName);
        if (tool == null) {
            return ToolResult.error("Функция не найдена");
        }
        return tool.execute(arguments);
    }

    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
}