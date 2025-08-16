package ru.comavp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import ru.comavp.email.EmailClient;

public class McpServerRunner {

    private EmailClient emailClient;
    private McpSyncServer mcpServer;

    public void run() {
        emailClient = new EmailClient();

        try {
            mcpServer = buildMcpServer();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            mcpServer.close();
        }
    }

    private McpSyncServer buildMcpServer() {
        var mcpServer = McpServer.sync(new StdioServerTransportProvider(new ObjectMapper()))
                .serverInfo("java-mcp-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .resources(false, false)
                        .tools(true)
                        .prompts(false)
                        .build())
                .build();
        mcpServer.addTool(getToolSpecification());
        return mcpServer;
    }

    private McpServerFeatures.SyncToolSpecification getToolSpecification() {
        String schema = """
                {
                  "type" : "object",
                  "properties" : {
                    "content" : {
                      "type" : "string"
                    }
                  }
                }
                        """;
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("mail-sender", "Tool for sending emails", schema),
                (exchange, arguments) -> {
                    emailClient.sendEmail((String) arguments.get("content"));
                    return new McpSchema.CallToolResult("Письмо успешно отправлено", false);
                });
    }
}
