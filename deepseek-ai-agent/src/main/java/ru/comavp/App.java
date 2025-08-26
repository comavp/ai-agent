package ru.comavp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class App {

    public static void main(String[] args) {
        DeepseekProperties properties = new DeepseekProperties();
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(properties.getAuthKey())
                .baseUrl("https://api.deepseek.com")
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage("Напиши программу на Java, которая решает квадратное уравнение")
                .model("deepseek-chat")
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        String answer = chatCompletion.choices().get(0).message().content().get();
        System.out.println(answer);
    }
}
