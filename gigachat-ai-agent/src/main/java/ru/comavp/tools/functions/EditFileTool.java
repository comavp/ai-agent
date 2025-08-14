package ru.comavp.tools.functions;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

public class EditFileTool implements Function<Map<String, Object>, String> {

    public static Function<Map<String, Object>, String> getEditFileTool() {
        return new EditFileTool();
    }

    @Override
    public String apply(Map<String, Object> input) {
        try {
            String path = (String) input.get("path");
            String oldStr = (String) input.get("old_str");
            String newStr = (String) input.get("new_str");

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
