package com.fahim1837.auth_gateway.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Setter
@Getter
@Builder
public class ResponseObject {

    private int statusCode;
    private String message;
    private Map<String, String> error;

    public String getResponse(int statusCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (statusCode >= 400) {
            return this.createErrorResponse();
        }
        String x = objectMapper.writeValueAsString(this);
        return x;

    }

    private String createErrorResponse() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("status", this.statusCode);
        map.put("message", this.message);
        map.put("error", this.error);
        String json = objectMapper.writeValueAsString(map);
        JsonNode jsonNode = objectMapper.readTree(json);
        System.out.println(jsonNode);
        Iterator<JsonNode> elements = jsonNode.iterator();
        while (elements.hasNext()) {
            JsonNode element = elements.next();
            if (element.isNull()) {
                elements.remove();
            }
        }
        return jsonNode.toString();
    }
}