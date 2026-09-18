package net.zytolga;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ResponseHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Random random = new Random();

    public ResponseHandler() {
        try (InputStream is = ResponseHandler.class.getResourceAsStream("/responses.json")) {
            if (is == null) {
                throw new IllegalStateException("responses.json not found on classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load responses.json", e);
        }
    }
}
