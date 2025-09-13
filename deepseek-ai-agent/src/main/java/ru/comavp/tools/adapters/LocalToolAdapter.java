package ru.comavp.tools.adapters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionTool;
import ru.comavp.tools.ToolDefinition;
import ru.comavp.tools.model.ToolResult;

import java.util.Map;

/**
 * @author Claude Code
 */
public class LocalToolAdapter implements ToolAdapter {

    private final ToolDefinition toolDefinition;

    public LocalToolAdapter(ToolDefinition toolDefinition) {
        this.toolDefinition = toolDefinition;
    }

    @Override
    public String getName() {
        return toolDefinition.getName();
    }

    @Override
    public String getDescription() {
        return toolDefinition.getDescription();
    }

    @Override
    public ChatCompletionTool toChatCompletionTool() {
        return ChatCompletionTool.ofFunction(ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name(toolDefinition.getName())
                        .description(toolDefinition.getDescription())
                        .parameters(toolDefinition.getParameters())
                        .build())
                .build());
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            JsonValue jsonArguments = JsonValue.from(arguments);
            String result = toolDefinition.getFunction().apply(jsonArguments);
            return ToolResult.success(result);
        } catch (Exception e) {
            return ToolResult.error("Error: " + e.getMessage());
        }
    }
}