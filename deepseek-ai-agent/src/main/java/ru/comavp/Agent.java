package ru.comavp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import ru.comavp.mcp.McpClientRunner;
import ru.comavp.tools.ToolManager;
import ru.comavp.tools.model.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Agent {

    private final OpenAIClient client;
    private final McpClientRunner mcpClient;
    private final Supplier<String> userMessage;

    private ObjectMapper mapper = new ObjectMapper();
    private ToolManager toolManager;
    private String lastToolCallId;

    public void run() {
        toolManager = new ToolManager(mcpClient);
        List<ChatCompletionMessage> dialog = new ArrayList<>();
        System.out.println("Chat with DeepSeek (use 'ctrl-c' to quit)");
        boolean readUserInput = true;
        while (true) {
            if (readUserInput) {
                System.out.print("\u001b[94mYou\u001b[0m: ");
                String userInput = userMessage.get();

                if (userInput == null || userInput.trim().isEmpty()) {
                    break;
                }

                dialog.add(ChatCompletionMessage.builder()
                        .content(userInput)
                        .role(JsonValue.from("user"))
                        .refusal("")
                        .build());
            }

            ChatCompletion response = sendUserMessage(dialog);
            if (response == null || CollectionUtils.isEmpty(response.choices())) {
                System.err.println("Error: No response from DeepSeek");
                continue;
            }
            ChatCompletionMessage assistantMessage = response.choices().get(0).message();
            dialog.add(assistantMessage);

            if (assistantMessage.content().isPresent() && StringUtils.isNotEmpty(assistantMessage.content().get())) {
                System.out.printf("\u001b[93mDeepSeek\u001b[0m: %s%n", assistantMessage.content().get());
                readUserInput = true;
            } else if (assistantMessage.toolCalls().isPresent()) {
                readUserInput = false;
                lastToolCallId = assistantMessage.toolCalls().get().get(0).asFunction().id();
                ToolResult result = executeTool(assistantMessage.toolCalls().get().get(0).asFunction().function());
                try {
                    dialog.add(ChatCompletionMessage.builder()
                            .role(JsonValue.from("tool"))
                            .content(mapper.writeValueAsString(result))
                            .refusal("")
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private ChatCompletion sendUserMessage(List<ChatCompletionMessage> dialog) {
        try {
            List<ChatCompletionTool> functions = toolManager.getAllChatCompletionTools();
            return client.chat().completions().create(ChatCompletionCreateParams.builder()
                    .model("deepseek-chat")
                    .maxCompletionTokens(1024)
                    .tools(functions)
                    .messages(dialog.stream()
                            .map(item -> switch (Objects.requireNonNull(item._role().convert(String.class))) {
                                case "user" ->
                                        ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                                                .content(item.content().get())
                                                .build());
                                case "assistant" -> item.toolCalls()
                                        .map(toolCalls -> ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                                                .content(item.content().get())
                                                .toolCalls(item.toolCalls().get())
                                                .build()))
                                        .orElse(ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                                                .content(item.content().get())
                                                .build()));
                                case "tool" ->
                                        ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                                                .content(item.content().get())
                                                .toolCallId(lastToolCallId)
                                                .build());
                                default -> throw new RuntimeException("Unknown user role");
                            })
                            .toList())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error during sending message", e);
        }
    }

    private ToolResult executeTool(ChatCompletionMessageFunctionToolCall.Function functionCall) {
        String toolName = functionCall.name();
        Map<String, Object> arguments;
        try {
            arguments = mapper.readValue(functionCall.arguments(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during parsing tool params", e);
        }
        
        try {
            System.out.printf("\u001b[92mtool\u001b[0m: %s(%s)%n", toolName, arguments.toString());
            return toolManager.executeTool(toolName, arguments);
        } catch (Exception e) {
            System.out.printf("\u001b[91mError executing tool\u001b[0m: %s%n", e.getMessage());
            return ToolResult.error("Error: " + e.getMessage());
        }
    }

}
