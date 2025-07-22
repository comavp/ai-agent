package ru.comavp.tools;

import chat.giga.model.completion.ChatFunctionParameters;

import java.util.Map;
import java.util.function.Function;

import static ru.comavp.tools.functions.ReadFileTool.getReadFileTool;
import static ru.comavp.tools.ToolSchemaUtils.getReadFileParameters;

public enum ToolDefinitions {

    READ_FILE_DEFINITION(
            "read_file",
            "Прочитай содержимое файла по заданному относительному пути. Используй это, когда хочешь посмотреть, " +
                    "что находится внутри файла. Не используй это с именами каталогов.",
            getReadFileParameters(),
            getReadFileTool());

    private final ToolDefinition value;

    ToolDefinitions(String name, String description, ChatFunctionParameters parameters, Function<Map<String, Object>, String> function) {
        this.value = new ToolDefinition(name, description, parameters, function);
    }

    public String getName() {
        return value.getName();
    }

    public String getDescription() {
        return value.getDescription();
    }

    public ChatFunctionParameters getParameters() {
        return value.getParameters();
    }

    public Function<Map<String, Object>, String> getFunction() {
        return value.getFunction();
    }
}
