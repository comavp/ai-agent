package ru.comavp;

import chat.giga.client.GigaChatClient;
import chat.giga.model.ModelName;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
public class Agent {

    private GigaChatClient client;
    private Supplier<String> userMessage;

    public void run() {
        List<ChatMessage> dialog = new ArrayList<>();
        System.out.println("Chat with GigaChat (use 'ctrl-c' to quit)");
        while (true) {
            System.out.print("\u001b[94mYou\u001b[0m: ");
            String userInput = userMessage.get();

            if (userInput == null || userInput.trim().isEmpty()) {
                break;
            }

            dialog.add(ChatMessage.builder()
                    .content(userInput)
                    .role(ChatMessage.Role.USER)
                    .build());

            CompletionResponse response = sendUserMessage(dialog);
            if (response == null || CollectionUtils.isEmpty(response.choices())
                    || response.choices().get(0).message().content().isEmpty()) {
                System.err.println("Error: No response from GigaChat");
                continue;
            }
            ChatMessage assistantMessage = response.choices().get(0).message().ofAssistantMessage();
            dialog.add(assistantMessage);

            System.out.printf("\u001b[93mGigaChat\u001b[0m: %s%n", assistantMessage.content());
        }
    }

    private CompletionResponse sendUserMessage(List<ChatMessage> dialog) {
        return client.completions(CompletionRequest.builder()
                .model(ModelName.GIGA_CHAT)
                .maxTokens(1024)
                .messages(dialog)
                .build());
    }
}
