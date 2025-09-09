package ru.comavp.tools;

import chat.giga.model.completion.ChatFunction;
import ru.comavp.tools.model.ToolResult;

import java.util.Map;

/**
 * @author Claude Code
 */
public interface UnifiedToolDefinition {

    String getName();
    String getDescription();
    ChatFunction toChatFunction();
    ToolResult execute(Map<String, Object> arguments);
}