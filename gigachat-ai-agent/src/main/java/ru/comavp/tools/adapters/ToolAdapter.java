package ru.comavp.tools.adapters;

import chat.giga.model.completion.ChatFunction;
import ru.comavp.tools.model.ToolResult;

import java.util.Map;

/**
 * @author Claude Code
 */
public interface ToolAdapter {

    String getName();
    String getDescription();
    ChatFunction toChatFunction();
    ToolResult execute(Map<String, Object> arguments);
}