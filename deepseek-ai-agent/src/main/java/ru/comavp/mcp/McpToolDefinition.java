package ru.comavp.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
@Builder
public class McpToolDefinition {
    private String name;
    private Function<McpSchema.CallToolRequest, McpSchema.CallToolResult> function;
}
