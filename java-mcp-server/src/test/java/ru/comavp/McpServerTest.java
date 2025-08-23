package ru.comavp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static ru.comavp.McpServerUtils.createInitializeRequest;

public class McpServerTest {

    private McpServerRunner mcpServerRunner;
    private ExecutorService executor;
    private ObjectMapper mapper = new ObjectMapper();

    private PipedOutputStream pipedOut;
    private PipedInputStream pipedIn;
    private PrintWriter writer;
    private InputStream originalSystemIn;

    private PipedInputStream outputPipe;
    private PipedOutputStream outputPipeOut;
    private BufferedReader outputReader;
    private PrintStream originalSystemOut;

    @BeforeEach
    public void init() throws IOException {
        executor = Executors.newCachedThreadPool();

        originalSystemIn = System.in;
        pipedIn = new PipedInputStream();
        pipedOut = new PipedOutputStream(pipedIn);
        writer = new PrintWriter(pipedOut, true);
        System.setIn(pipedIn);

        originalSystemOut = System.out;
        outputPipe = new PipedInputStream();
        outputPipeOut = new PipedOutputStream(outputPipe);
        outputReader = new BufferedReader(new InputStreamReader(outputPipe));
        System.setOut(new PrintStream(outputPipeOut, true));

        mcpServerRunner = new McpServerRunner();
        startServer();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        stopServer();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
    }

    @Test
    public void testInitRequest() throws IOException {
        var response = sendRequestAndWaitForResponse(createInitializeRequest());

        assertNotNull(response, "Server should respond to initialize request");
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(0, response.get("id").asInt());

        JsonNode result = response.get("result");
        assertNotNull(result, "Response should contain result");
        assertEquals("2024-11-05", result.get("protocolVersion").asText());

        JsonNode capabilities = result.get("capabilities");
        assertNotNull(capabilities, "Response should contain capabilities");
        assertNotNull(capabilities.get("logging"));
        assertNotNull(capabilities.get("prompts"));
        assertFalse(capabilities.get("prompts").get("listChanged").asBoolean());
        assertNotNull(capabilities.get("resources"));
        assertFalse(capabilities.get("resources").get("subscribe").asBoolean());
        assertFalse(capabilities.get("resources").get("listChanged").asBoolean());
        assertNotNull(capabilities.get("tools"));
        assertFalse(capabilities.get("tools").get("listChanged").asBoolean());

        JsonNode serverInfo = result.get("serverInfo");
        assertNotNull(serverInfo, "Response should contain serverInfo");
        assertNotNull(serverInfo.get("name"), "Server info should contain name");
        assertEquals("java-mcp-server", serverInfo.get("name").asText());
        assertNotNull(serverInfo.get("version"), "Server info should contain version");
        assertEquals("1.0.0", serverInfo.get("version").asText());
    }

    private void startServer() {
        executor.submit(() -> mcpServerRunner.run());
    }

    private void stopServer() {
        mcpServerRunner.close();
    }

    private JsonNode sendRequestAndWaitForResponse(ObjectNode request) throws IOException {
        writer.println(mapper.writeValueAsString(request));
        writer.flush();

        String mcpServerResponse = outputReader.readLine();
        return mapper.readTree(mcpServerResponse);
    }
}
