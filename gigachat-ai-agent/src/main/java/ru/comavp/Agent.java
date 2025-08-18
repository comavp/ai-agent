package ru.comavp;

import chat.giga.client.GigaChatClient;
import chat.giga.http.client.HttpClientException;
import chat.giga.model.ModelName;
import chat.giga.model.completion.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import ru.comavp.mcp.McpClientRunner;
import ru.comavp.mcp.McpToolDefinition;
import ru.comavp.tools.ToolDefinitions;
import ru.comavp.tools.model.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Agent {

    private final GigaChatClient client;
    private final McpClientRunner mcpClient;
    private final Supplier<String> userMessage;

    private ObjectMapper mapper = new ObjectMapper();

    public void run() {
        var mcpToolsResult = mcpClient.getToolsList();
        var mcpFunctions = getMcpChatFunctions(mcpToolsResult);
        var mcpFunctionsCallersMap = buildMcpToolsDefinitionsMap(mcpToolsResult);
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

            CompletionResponse response = sendUserMessage(dialog, mcpFunctions);
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
                ToolResult result = executeTool(assistantMessage.functionCall(), mcpFunctionsCallersMap);
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

    private Map<String, McpToolDefinition> buildMcpToolsDefinitionsMap(McpSchema.ListToolsResult result) {
       return result.tools()
               .stream()
               .map(mcpTool -> new McpSchema.CallToolRequest(mcpTool.name(), mcpTool.inputSchema().properties()))
               .collect(Collectors.toMap(
                       McpSchema.CallToolRequest::name,
                       callToolRequest -> McpToolDefinition.builder()
                               .name(callToolRequest.name())
                               .function(t -> mcpClient.executeTool(callToolRequest))
                               .build()
               ));
    }

    private CompletionResponse sendUserMessage(List<ChatMessage> dialog, List<ChatFunction> mcpFunctions) {
        try {
            List<ChatFunction> functions = Stream.concat(getChatFunctions().stream(), mcpFunctions.stream()).toList();
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

    private ToolResult executeTool(ChoiceMessageFunctionCall functionCall, Map<String, McpToolDefinition> mcpFunctions) {
        String toolName = functionCall.name();
        Map<String, Object> arguments = functionCall.arguments();
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
        } /*else if (mcpFunctions.containsKey(toolName)) {
            var mcpToolDefinition = mcpFunctions.get(toolName);
            var mcpToolCaller = mcpToolDefinition.getFunction();
            try {
                System.out.printf("\u001b[92mtool\u001b[0m: %s(%s)%n", toolName, arguments.toString());
                return mapMcpToolCallResult(mcpToolCaller.apply(new McpSchema.CallToolRequest(toolName, arguments)));
            } catch (Exception e) {
                System.out.printf("\u001b[91mError executing tool\u001b[0m: %s%n", e.getMessage());
                return ToolResult.error("Error: " + e.getMessage());
            }
        }*/ else {
            System.out.printf("\u001b[91mError\u001b[0m: Tool '%s' not found%n", toolName);
            return ToolResult.error("Функция не найдена");
        }
    }

    private ToolResult mapMcpToolCallResult(McpSchema.CallToolResult callToolResult) {
        return new ToolResult("Вызов MCP тула завершен", callToolResult.isError());
    }

    private List<ChatFunction> getChatFunctions() {
        return Stream.of(ToolDefinitions.values())
                .map(toolDefinition -> ChatFunction.builder()
                        .name(toolDefinition.getName())
                        .description(toolDefinition.getDescription())
                        .parameters(toolDefinition.getParameters())
                        .build())
                .toList();
    }

    private List<ChatFunction> getMcpChatFunctions(McpSchema.ListToolsResult mcpTools) {
        return mcpTools.tools()
                .stream()
                .map(mcpTool -> ChatFunction.builder()
                        .name(mcpTool.name())
                        .description(mcpTool.description())
                        .parameters(mapMcpToolParamsToChatFunctionParams(mcpTool.inputSchema()))
                        .build())
                .toList();
    }

    private ChatFunctionParameters mapMcpToolParamsToChatFunctionParams(McpSchema.JsonSchema jsonSchema) {
        return ChatFunctionParameters.builder()
                .type(jsonSchema.type())
                .property("content", ChatFunctionParametersProperty.builder()
                        .type("string")
                        .build())
                .build();
    }
}
