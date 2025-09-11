package ru.comavp.tools.adapters;

import chat.giga.model.completion.ChatFunction;
import chat.giga.model.completion.ChatFunctionParameters;
import chat.giga.model.completion.ChatFunctionParametersProperty;
import io.modelcontextprotocol.spec.McpSchema;
import ru.comavp.tools.model.ToolResult;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Claude Code
 */
public class McpToolAdapter implements ToolAdapter {

    private final McpSchema.Tool mcpTool;
    private final Function<McpSchema.CallToolRequest, McpSchema.CallToolResult> executor;

    public McpToolAdapter(McpSchema.Tool mcpTool, Function<McpSchema.CallToolRequest, McpSchema.CallToolResult> executor) {
        this.mcpTool = mcpTool;
        this.executor = executor;
    }

    @Override
    public String getName() {
        return mcpTool.name();
    }

    @Override
    public String getDescription() {
        return mcpTool.description();
    }

    @Override
    public ChatFunction toChatFunction() {
        return ChatFunction.builder()
                .name(mcpTool.name())
                .description(mcpTool.description())
                .parameters(mapMcpToolParamsToChatFunctionParams(mcpTool.inputSchema()))
                .build();
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(mcpTool.name(), arguments);
            McpSchema.CallToolResult result = executor.apply(request);
            return new ToolResult("Вызов MCP тула завершен", result.isError());
        } catch (Exception e) {
            return ToolResult.error("Error: " + e.getMessage());
        }
    }

    private ChatFunctionParameters mapMcpToolParamsToChatFunctionParams(McpSchema.JsonSchema jsonSchema) {
        return ChatFunctionParameters.builder()
                .type(jsonSchema.type())
                .property("content", ChatFunctionParametersProperty.builder()
                        .type("string")
                        .build())
                .build();
    }
}