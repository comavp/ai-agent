package ru.comavp.tools.adapters;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionTool;
import io.modelcontextprotocol.spec.McpSchema;
import ru.comavp.tools.model.ToolResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
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
    public ChatCompletionTool toChatCompletionTool() {
        return ChatCompletionTool.ofFunction(ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name(mcpTool.name())
                        .description(mcpTool.description())
                        .parameters(mapMcpToolParamsToChatFunctionParams(mcpTool.inputSchema()))
                        .build())
                .build());
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

    private FunctionParameters mapMcpToolParamsToChatFunctionParams(McpSchema.JsonSchema jsonSchema) {
        return FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from(jsonSchema.type()))
                .putAdditionalProperty("properties", JsonValue.from(jsonSchema.properties()))
                .putAdditionalProperty("required", JsonValue.from(Objects.isNull(jsonSchema.required())
                        ? new ArrayList<>() : jsonSchema.required()))
                .putAdditionalProperty("additionalProperties", JsonValue.from(
                        Objects.nonNull(jsonSchema.additionalProperties()) ? jsonSchema.additionalProperties() : false))
                .putAdditionalProperty("$defs", JsonValue.from(
                        Objects.nonNull(jsonSchema.defs()) ? jsonSchema.defs() : Map.of()))
                .putAdditionalProperty("definitions", JsonValue.from(
                        Objects.nonNull(jsonSchema.definitions()) ? jsonSchema.definitions() : Map.of()))
                .build();
    }
}