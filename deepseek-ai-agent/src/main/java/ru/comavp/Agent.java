package ru.comavp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Agent {

    private final OpenAIClient client;
    private final Supplier<String> userMessage;

    public void run() {
        List<ChatCompletionMessage> dialog = new ArrayList<>();
        System.out.println("Chat with GigaChat (use 'ctrl-c' to quit)");
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
                System.err.println("Error: No response from GigaChat");
                continue;
            }
            ChatCompletionMessage assistantMessage = response.choices().get(0).message();
            dialog.add(assistantMessage);

            if (assistantMessage.content().isPresent()) {
                System.out.printf("\u001b[93mGigaChat\u001b[0m: %s%n", assistantMessage.content().get());
            }
        }
    }

    private ChatCompletion sendUserMessage(List<ChatCompletionMessage> dialog) {
        try {
            return client.chat().completions().create(ChatCompletionCreateParams.builder()
                    .model("deepseek-chat")
                    .maxCompletionTokens(1024)
                    .messages(dialog.stream()
                            .map(item -> switch(Objects.requireNonNull(item._role().convert(String.class))) {
                                case "user" -> ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder()
                                        .content(item.content().get())
                                        .build());
                                case "assistant" -> ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                                        .content(item.content().get())
                                        .build());
                                default -> throw new RuntimeException("Unknown user role");
                            })
                            .toList())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error during sending message", e);
        }
    }
}
