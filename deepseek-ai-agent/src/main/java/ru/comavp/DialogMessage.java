package ru.comavp;

import com.openai.models.chat.completions.ChatCompletionMessage;

/**
 * @author Claude Code
 */
public class DialogMessage {
    private final ChatCompletionMessage message;
    private final String toolCallId;

    public DialogMessage(ChatCompletionMessage message) {
        this.message = message;
        this.toolCallId = null;
    }

    public DialogMessage(ChatCompletionMessage message, String toolCallId) {
        this.message = message;
        this.toolCallId = toolCallId;
    }

    public ChatCompletionMessage getMessage() {
        return message;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public boolean hasToolCallId() {
        return toolCallId != null;
    }
}