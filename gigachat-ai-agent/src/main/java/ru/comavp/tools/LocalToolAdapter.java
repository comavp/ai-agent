package ru.comavp.tools;

import chat.giga.model.completion.ChatFunction;
import ru.comavp.tools.model.ToolResult;

import java.util.Map;

/**
 * @author Claude Code
 */
public class LocalToolAdapter implements UnifiedToolDefinition {

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
    public ChatFunction toChatFunction() {
        return ChatFunction.builder()
                .name(toolDefinition.getName())
                .description(toolDefinition.getDescription())
                .parameters(toolDefinition.getParameters())
                .build();
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        try {
            String result = toolDefinition.getFunction().apply(arguments);
            return ToolResult.success(result);
        } catch (Exception e) {
            return ToolResult.error("Error: " + e.getMessage());
        }
    }
}