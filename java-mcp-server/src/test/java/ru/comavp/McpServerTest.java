package ru.comavp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.comavp.email.EmailClient;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static ru.comavp.McpServerUtils.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private EmailClient emailClient;

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

        mcpServerRunner = new McpServerRunner(emailClient);
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

    @Test
    public void testListToolsRequest() throws IOException {
        sendRequest(createInitializedRequest());
        var response = sendRequestAndWaitForResponse(createListToolsRequest());

        assertNotNull(response, "Server should respond to initialize request");
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(0, response.get("id").asInt());

        JsonNode result = response.get("result");
        assertNotNull(result, "Response should contain result");
        assertNotNull(result.get("tools"), "Result should contain tools");

        JsonNode tools = result.get("tools");
        assertEquals(1, tools.size());

        JsonNode tool = tools.get(0);
        assertEquals("mail-sender", tool.get("name").asText());
        assertEquals("Tool for sending emails", tool.get("description").asText());
        assertNotNull(tool.get("inputSchema"));

        JsonNode inputSchema = tool.get("inputSchema");
        assertEquals("object", inputSchema.get("type").asText());
        assertNotNull(inputSchema.get("properties"));

        JsonNode properties = inputSchema.get("properties");
        assertEquals(1, properties.size());
        assertNotNull(properties.get("content"));

        JsonNode content = properties.get("content");
        assertEquals("string", content.get("type").asText());
    }

    @Test
    public void testCallToolRequest() throws IOException {
        sendRequest(createInitializedRequest());
        var response = sendRequestAndWaitForResponse(createCallToolRequest());

        assertNotNull(response, "Server should respond to initialize request");
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(0, response.get("id").asInt());

        JsonNode result = response.get("result");
        assertNotNull(result, "Response should contain result");
        assertFalse(result.get("isError").asBoolean());
        assertNotNull(result.get("content"), "Result should contain content");

        JsonNode content = result.get("content");
        assertEquals(1, content.size());

        JsonNode contentItem = content.get(0);
        assertEquals(2, contentItem.size());
        assertEquals("text", contentItem.get("type").asText());
        assertEquals("Письмо успешно отправлено", contentItem.get("text").asText());
    }

    private void startServer() {
        executor.submit(() -> mcpServerRunner.run());
    }

    private void stopServer() {
        mcpServerRunner.close();
    }

    private void sendRequest(ObjectNode request) throws JsonProcessingException {
        writer.println(mapper.writeValueAsString(request));
        writer.flush();
    }

    private JsonNode sendRequestAndWaitForResponse(ObjectNode request) throws IOException {
        sendRequest(request);
        String mcpServerResponse = outputReader.readLine();
        return mapper.readTree(mcpServerResponse);
    }
}
