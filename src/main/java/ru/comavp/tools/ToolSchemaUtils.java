package ru.comavp.tools;

import chat.giga.model.completion.ChatFunctionParameters;
import chat.giga.model.completion.ChatFunctionParametersProperty;

import java.util.List;

public final class ToolSchemaUtils {

    private ToolSchemaUtils() {}

    public static ChatFunctionParameters getReadFileParameters() {
        return ChatFunctionParameters.builder()
                .type("object")
                .property("path", ChatFunctionParametersProperty.builder()
                        .type("string")
                        .description("Относительный путь файла в рабочем каталоге.")
                        .build())
                .required(List.of("path"))
                .build();
    }

    public static ChatFunctionParameters getListFilesParameters() {
        return ChatFunctionParameters.builder()
                .type("object")
                .property("path", ChatFunctionParametersProperty.builder()
                        .type("string")
                        .description("Опциональный относительный путь для списка файлов. По умолчанию текущая директория")
                        .build())
                .build();
    }
}
