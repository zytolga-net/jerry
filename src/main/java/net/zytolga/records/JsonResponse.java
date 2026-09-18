package net.zytolga.records;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;

public record JsonResponse(JsonNode body, int status, boolean success) {
    @NotNull
    @Contract("_ -> new")
    public static JsonResponse fromHttpResponse(@NotNull HttpResponse<String> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode body = objectMapper.readTree(response.body());
        boolean success;
        if (body.isArray()) success = response.statusCode() == 200;
        else if (body.has("Status")) success = body.get("Status").asBoolean();
        else success = response.statusCode() == 200;
        return new JsonResponse(body, response.statusCode(), success);
    }
}
