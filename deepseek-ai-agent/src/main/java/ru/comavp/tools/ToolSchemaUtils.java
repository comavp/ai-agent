package ru.comavp.tools;

import com.openai.core.JsonValue;
import com.openai.models.FunctionParameters;

import java.util.List;
import java.util.Map;

public final class ToolSchemaUtils {

    private ToolSchemaUtils() {}

    public static FunctionParameters getReadFileParameters() {
        return FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of("path", Map.of(
                        "type", "string",
                        "description", "Относительный путь файла в рабочем каталоге."))))
                .putAdditionalProperty("required", JsonValue.from(List.of("path")))
                .build();
    }

    public static FunctionParameters getListFilesParameters() {
        return FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of("path", Map.of(
                        "type", "string",
                        "description", "Опциональный относительный путь для списка файлов. По умолчанию текущая директория"))))
                .build();
    }

    public static FunctionParameters getEditFileParameters() {
        return FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                        "path", Map.of(
                                "type", "string",
                                "description", "Путь к файлу"),
                        "old_str", Map.of(
                                "type", "string",
                                "description", "Текст для поиска - должен точно совпадать и иметь только одно совпадение"),
                        "new_str", Map.of(
                                "type", "string",
                                "description", "Текст для замены old_str")
                )))
                .putAdditionalProperty("required", JsonValue.from(List.of("path", "old_str", "new_str")))
                .build();
    }
}
