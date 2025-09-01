package ru.comavp.tools;

import com.openai.core.JsonValue;
import com.openai.models.FunctionParameters;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class ToolDefinition {

    private String name;
    private String description;
    private FunctionParameters parameters;
    private Function<JsonValue, String> function;
}
