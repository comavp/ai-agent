package ru.comavp;

import chat.giga.client.GigaChatClient;
import chat.giga.http.client.HttpClientException;
import chat.giga.model.ModelName;
import chat.giga.model.completion.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import ru.comavp.mcp.McpClientRunner;
import ru.comavp.tools.UnifiedToolManager;
import ru.comavp.tools.model.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Agent {

    private final GigaChatClient client;
    private final McpClientRunner mcpClient;
    private final Supplier<String> userMessage;

    private ObjectMapper mapper = new ObjectMapper();
    private UnifiedToolManager toolManager;

    public void run() {
        toolManager = new UnifiedToolManager(mcpClient);
        List<ChatMessage> dialog = new ArrayList<>();
        System.out.println("Chat with GigaChat (use 'ctrl-c' to quit)");
        boolean readUserInput = true;
        while (true) {
            if (readUserInput) {
                System.out.print("\u001b[94mYou\u001b[0m: ");
                String userInput = userMessage.get();

                if (userInput == null || userInput.trim().isEmpty()) {
                    break;
                }

                dialog.add(ChatMessage.builder()
                        .content(userInput)
                        .role(ChatMessage.Role.USER)
                        .build());
            }

            CompletionResponse response = sendUserMessage(dialog);
            if (response == null || CollectionUtils.isEmpty(response.choices())) {
                System.err.println("Error: No response from GigaChat");
                continue;
            }
            ChoiceMessage assistantMessage = response.choices().get(0).message();
            dialog.add(ChatMessage.of(assistantMessage));

            if (!assistantMessage.content().isEmpty()) {
                System.out.printf("\u001b[93mGigaChat\u001b[0m: %s%n", assistantMessage.content());
                readUserInput = true;
            } else if (assistantMessage.functionCall() != null) {
                readUserInput = false;
                ToolResult result = executeTool(assistantMessage.functionCall());
                try {
                    dialog.add(ChatMessage.builder()
                            .role(ChatMessage.Role.FUNCTION)
                            .content(mapper.writeValueAsString(result))
                            .name(assistantMessage.functionCall().name())
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private CompletionResponse sendUserMessage(List<ChatMessage> dialog) {
        try {
            List<ChatFunction> functions = toolManager.getAllChatFunctions();
            return client.completions(CompletionRequest.builder()
                    .model(ModelName.GIGA_CHAT)
                    .maxTokens(1024)
                    .functions(functions)
                    .messages(dialog)
                    .build());
        } catch (HttpClientException e) {
            throw new RuntimeException(String.format("Sending message failed with code '%s' and body '%s'",
                    e.statusCode(), e.bodyAsString()), e);
        } catch (Exception e) {
            throw new RuntimeException("Error during sending message", e);
        }
    }

    private ToolResult executeTool(ChoiceMessageFunctionCall functionCall) {
        String toolName = functionCall.name();
        Map<String, Object> arguments = functionCall.arguments();
        
        try {
            System.out.printf("\u001b[92mtool\u001b[0m: %s(%s)%n", toolName, arguments.toString());
            return toolManager.executeTool(toolName, arguments);
        } catch (Exception e) {
            System.out.printf("\u001b[91mError executing tool\u001b[0m: %s%n", e.getMessage());
            return ToolResult.error("Error: " + e.getMessage());
        }
    }

}
