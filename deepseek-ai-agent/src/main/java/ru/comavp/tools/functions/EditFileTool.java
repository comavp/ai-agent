package ru.comavp.tools.functions;

import com.openai.core.JsonObject;
import com.openai.core.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class EditFileTool implements Function<JsonValue, String> {

    public static Function<JsonValue, String> getEditFileTool() {
        return new EditFileTool();
    }

    @Override
    public String apply(JsonValue input) {
        try {
            String path = ((JsonObject) input).values().get("path").asStringOrThrow();
            String oldStr = ((JsonObject) input).values().get("old_str").asStringOrThrow();
            String newStr = ((JsonObject) input).values().get("new_str").asStringOrThrow();

            if (StringUtils.isEmpty(path) || oldStr.equals(newStr)) {
                throw new RuntimeException("Некорректные входные параметры");
            }

            Path filePath = Paths.get(path);

            try {
                String content = Files.readString(filePath);
                String newContent = content.replace(oldStr, newStr);
                if (content.equals(newContent) && StringUtils.isNotEmpty(oldStr)) {
                    throw new RuntimeException("old_str не найден в файле");
                }

                Files.writeString(filePath, newContent);
                return String.format("Файл %s успешно отредактирован", path);
            } catch (NoSuchFileException e) {
                if (oldStr.isEmpty()) {
                    return createNewFile(path, newStr);
                }
               throw new RuntimeException("Файл не найден: " + path);
            }
        } catch (Exception e) {
            return "Error editing file: " + e.getMessage();
        }
    }

    private static String createNewFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.writeString(path, content);
            return "Файл успешно создан: " + filePath;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании файла: " + e.getMessage(), e);
        }
    }
}
