package ru.comavp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.chat.completions.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import ru.comavp.tools.ToolDefinitions;
import ru.comavp.tools.model.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Agent {

    private final OpenAIClient client;
    private final Supplier<String> userMessage;

    private ObjectMapper mapper = new ObjectMapper();

    public void run() {
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
                ToolResult result = executeTool(assistantMessage.toolCalls().get().get(0).asFunction().function());
                try {
                    dialog.add(ChatCompletionMessage.builder()
                            .role(JsonValue.from("tool"))
                            .content(mapper.writeValueAsString(result))
                            .refusal("")
                            .toolCalls(assistantMessage.toolCalls().get())
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private ChatCompletion sendUserMessage(List<ChatCompletionMessage> dialog) {
        try {
            List<ChatCompletionTool> functions = getChatFunctions();
            return client.chat().completions().create(ChatCompletionCreateParams.builder()
                    .model("deepseek-chat")
                    .maxCompletionTokens(1024)
                    .tools(functions)
                    .messages(dialog.stream()
                            .map(item -> {
                                var obj = switch(Objects.requireNonNull(item._role().convert(String.class))) {
                                    case "user" -> ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                                            .content(item.content().get())
                                            .build());
                                    case "assistant" -> ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                                            .content(item.content().get())
                                            //.toolCalls(item.toolCalls().get())
                                            .build());
                                    case "tool" -> ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                                            .content(item.content().get())
                                            .toolCallId(item.toolCalls().get().get(0).asFunction().id())
                                            .build());
                                    default -> throw new RuntimeException("Unknown user role");
                                };
                                if (obj.isAssistant() && item.toolCalls().isPresent()) {
                                    return ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                                            .content(item.content().get())
                                            .toolCalls(item.toolCalls().get())
                                            .build());
                                }
                                return obj;
                            })
                            .toList())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error during sending message", e);
        }
    }

    private ToolResult executeTool(ChatCompletionMessageFunctionToolCall.Function functionCall) {
        String toolName = functionCall.name();
        JsonValue arguments = null;
        try {
            arguments = JsonValue.from(mapper.readTree(functionCall.arguments()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during parsing tool params", e);
        }
        var functionOpt = Stream.of(ToolDefinitions.values())
                .filter(item -> item.getName().equals(toolName))
                .findFirst();

        if (functionOpt.isPresent()) {
            try {
                System.out.printf("\u001b[92mtool\u001b[0m: %s(%s)%n", toolName, arguments.toString());
                return ToolResult.success(functionOpt.get()
                        .getFunction()
                        .apply(arguments));
            } catch (Exception e) {
                System.out.printf("\u001b[91mError executing tool\u001b[0m: %s%n", e.getMessage());
                return ToolResult.error("Error: " + e.getMessage());
            }
        } else {
            System.out.printf("\u001b[91mError\u001b[0m: Tool '%s' not found%n", toolName);
            return ToolResult.error("Функция не найдена");
        }
    }

    private List<ChatCompletionTool> getChatFunctions() {
        return Stream.of(ToolDefinitions.values())
                .map(toolDefinition -> ChatCompletionTool.ofFunction(ChatCompletionFunctionTool.builder()
                        .function(FunctionDefinition.builder()
                                .name(toolDefinition.getName())
                                .description(toolDefinition.getDescription())
                                .parameters(toolDefinition.getParameters())
                                .build())
                        .build()))
                .toList();
    }
}
