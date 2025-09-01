package ru.comavp.tools.functions;

import com.openai.core.JsonObject;
import com.openai.core.JsonValue;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class ReadFileTool implements Function<JsonValue, String> {

    public static Function<JsonValue, String> getReadFileTool() {
        return new ReadFileTool();
    }

    @Override
    public String apply(JsonValue input) {
        try {
            Path path = Paths.get(((JsonObject) input).values().get("path").asStringOrThrow());
            return StringEscapeUtils.escapeJson(Files.readString(path));
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }
}
