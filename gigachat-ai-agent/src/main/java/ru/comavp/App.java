package ru.comavp;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.Scope;
import ru.comavp.mcp.McpClientRunner;

import java.util.Scanner;
import java.util.function.Supplier;

public class App {

    public static void main(String[] args) {
        GigaChatProperties properties = new GigaChatProperties();
        GigaChatClient client = GigaChatClient.builder()
                .authClient(AuthClient.builder()
                        .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                                .scope(Scope.GIGACHAT_API_PERS)
                                .authKey(properties.getAuthKey())
                                .build())
                        .build())
                .build();

        Scanner scanner = new Scanner(System.in);
        Supplier<String> userMessage = () -> {
            try {
                return scanner.nextLine();
            } catch (Exception e) {
                System.err.println("Error during reading user input: " + e.getMessage());
                return null;
            }
        };

        Agent agent = new Agent(client,
                new McpClientRunner(properties.getUserName(), properties.getPassword(), properties.getRecipient()),
                userMessage);
        agent.run();
    }
}
