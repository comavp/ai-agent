package ru.comavp.tools;

import chat.giga.model.completion.ChatFunctionParameters;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class ToolDefinition {

    private String name;
    private String description;
    private ChatFunctionParameters parameters;
    private Function<Map<String, Object>, String> function;
}
