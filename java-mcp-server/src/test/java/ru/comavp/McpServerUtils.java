package ru.comavp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class McpServerUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    private McpServerUtils() {}

    public static ObjectNode createInitializeRequest() {
        ObjectNode request = mapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "initialize");
        request.put("id", 0);

        ObjectNode params = mapper.createObjectNode();
        params.put("protocolVersion", "2025-06-18");
        params.putObject("capabilities");

        ObjectNode clientInfo = mapper.createObjectNode();
        clientInfo.put("name", "test-client");
        clientInfo.put("version", "1.0.0");
        params.set("clientInfo", clientInfo);

        request.set("params", params);

        return request;
    }

    public static ObjectNode createInitializedRequest() {
        ObjectNode request = mapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "notifications/initialized");

        return request;
    }

    public static ObjectNode createListToolsRequest() {
        ObjectNode request = mapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 0);
        request.put("method", "tools/list");

        ObjectNode params = mapper.createObjectNode();

        request.set("params", params);

        return request;
    }
}
