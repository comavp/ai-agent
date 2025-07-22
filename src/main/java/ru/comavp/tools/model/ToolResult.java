package ru.comavp.tools.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    private String content;
    @JsonIgnore
    private boolean isError;

    public static ToolResult success(String content) {
        return new ToolResult(content, false);
    }

    public static ToolResult error(String error) {
        return new ToolResult(error, true);
    }
}
