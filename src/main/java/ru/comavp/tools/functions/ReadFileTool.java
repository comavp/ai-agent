package ru.comavp.tools.functions;

import org.apache.commons.text.StringEscapeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

public class ReadFileTool implements Function<Map<String, Object>, String> {

    public static Function<Map<String, Object>, String> getReadFileTool() {
        return new ReadFileTool();
    }

    @Override
    public String apply(Map<String, Object> input) {
        try {
            Path path = Paths.get((String) input.get("path"));
            return StringEscapeUtils.escapeJson(Files.readString(path));
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }
}
