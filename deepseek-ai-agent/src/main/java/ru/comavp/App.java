package ru.comavp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import java.util.Scanner;
import java.util.function.Supplier;

public class App {

    public static void main(String[] args) {
        DeepseekProperties properties = new DeepseekProperties();
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(properties.getAuthKey())
                .baseUrl("https://api.deepseek.com")
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

        Agent agent = new Agent(client, userMessage);
        agent.run();
    }
}
