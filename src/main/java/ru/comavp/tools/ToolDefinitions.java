package ru.comavp.tools;

import chat.giga.model.completion.ChatFunctionParameters;

import java.util.Map;
import java.util.function.Function;

import static ru.comavp.tools.ToolSchemaUtils.getListFilesParameters;
import static ru.comavp.tools.functions.ListFilesTool.getListFilesTool;
import static ru.comavp.tools.functions.ReadFileTool.getReadFileTool;
import static ru.comavp.tools.ToolSchemaUtils.getReadFileParameters;

public enum ToolDefinitions {

    READ_FILE_DEFINITION(
            "read_file",
            "Прочитай содержимое файла по заданному относительному пути. Используй это, когда хочешь посмотреть, " +
                    "что находится внутри файла. Не используй это с именами каталогов.",
            getReadFileParameters(),
            getReadFileTool()),
    LIST_FILES_DEFINITION(
            "list_files",
            "Выводит список файлов и директорий по указанному пути. Если путь не указан, выводит файлы в текущей директории.",
            getListFilesParameters(),
            getListFilesTool()
    );

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
