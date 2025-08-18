package ru.comavp.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

public class McpClientRunner {

    private McpSyncClient mcpClient;

    public McpClientRunner() {
        ServerParameters params = ServerParameters.builder("java")
                .args("-jar", "D:\\JavaProjects\\ai-agent\\ai-agent\\java-mcp-server\\target\\java-mcp-server-1.0-SNAPSHOT.jar")
                .build();
        mcpClient = McpClient.sync(new StdioClientTransport(params)).build();
        mcpClient.initialize();
    }

    public McpSchema.ListToolsResult getToolsList() {
        return mcpClient.listTools();
    }

    public McpSchema.CallToolResult executeTool(McpSchema.CallToolRequest callToolRequest) {
        return mcpClient.callTool(callToolRequest);
    }
}
