package ru.comavp.tools.adapters;

import com.openai.models.chat.completions.ChatCompletionTool;
import ru.comavp.tools.model.ToolResult;

import java.util.Map;

/**
 * @author Claude Code
 */
public interface ToolAdapter {

    String getName();
    String getDescription();
    ChatCompletionTool toChatCompletionTool();
    ToolResult execute(Map<String, Object> arguments);
}