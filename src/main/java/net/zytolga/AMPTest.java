package net.zytolga;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base32;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static net.zytolga.Jerry.logger;

public class AMPTest {
    private final String session;

    public AMPTest() throws Exception {
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");

        try (HttpClient client = HttpClient.newHttpClient()) {
            ObjectMapper objectMapper = new ObjectMapper();

            String loginBody = """
                        {"USERNAME": "%s","PASSWORD": "%s","token": "","RememberMe": true}
                    """.formatted(username, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://amp.zytolga.net/API/Core/Login"))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode data = objectMapper.readTree(response.body());
            if (!data.has("sessionID") || data.get("sessionID").asString().trim().isEmpty()) {
                throw new Exception("Invalid session ID returned from AMP");
            }
            session = data.get("sessionID").asString();
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
    }

    public String getSession() {
        return session;
    }

    public void logout() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://amp.zytolga.net/API/Core/Logout"))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Jerry/1.0")
                    .header("Authorization", "Bearer " + session)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Logged out of AMP");
            } else {
                logger.error(response.body());
            }
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
    }

    private String generate2FA(String secret) throws Exception {
        try {
            CodeGenerator gen = new DefaultCodeGenerator();
            TimeProvider timeProvider = new NtpTimeProvider("pool.ntp.org");
            Base32 base32 = new Base32();

            return gen.generate(base32.encodeAsString(secret.getBytes()), timeProvider.getTime());
        } catch (UnknownHostException | CodeGenerationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
    }
}
