package net.zytolga;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.commons.codec.binary.Base32;
import org.jetbrains.annotations.NotNull;
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

public class AMPService {
    private final String ampurl;
    private final String username;
    private final String password;
    private String sessionID;

    public AMPService(String ampurl, String username, String password) {
        this.ampurl = ampurl;
        this.username = username;
        this.password = password;
    }

    public void login() throws Exception {
        String loginBody = """
                    {"USERNAME": "%s","PASSWORD": "%s","token": "","RememberMe": true}
                """.formatted(username, password);

        JsonNode data = httpPost("/API/Core/Login", loginBody);
        if (!data.has("sessionID") || data.get("sessionID").asString().trim().isEmpty()) {
            throw new Exception("Invalid session ID returned from AMP");
        }
        sessionID = data.get("sessionID").asString();
        logger.info("Session ID = " + sessionID);
    }

    public void logout() {
        httpPost("/API/Core/Logout", "{}");
    }

//    public List<AMPInstance> GetInstances() {
//
//    }
//    public List<AMPInstance> GetInstances(boolean ForceIncludeSelf) {
//
//    }
//    public AMPInstance GetInstance(String instanceID) {
//        return null;
//    }
//
    public boolean StartInstance(String instanceName) {
        String postContent = """
                    {"InstanceName": "%s"}
                """.formatted(instanceName);
        JsonNode data = httpPost("/API/ADSModule/StartInstance", postContent);
        logger.info(data.toString());
        return data.get("Status").asBoolean();
    }
    public boolean RestartInstance(String instanceName) {
        String postContent = """
                    {"InstanceName": "%s"}
                """.formatted(instanceName);
        JsonNode data = httpPost("/API/ADSModule/RestartInstance", postContent);
        logger.info(data.toString());
        return data.get("Status").asBoolean();
    }
    public boolean StopInstance(String instanceName) {
        String postContent = """
                    {"InstanceName": "%s"}
                """.formatted(instanceName);
        JsonNode data = httpPost("/API/ADSModule/StopInstance", postContent);
        logger.info(data.toString());
        return data.get("Status").asBoolean();
    }
//
//    public void CreateUser(String username) {
//
//    }
//    public void ChangeUserPassword(String username, String oldPassword, String newPassword, String twoFactorPin) {
//
//    }
//    public void SetAMPUserRoleMembership(String userId, String roleId, boolean isMember) {
//
//    }
//    public void GetAMPUserInfo(String username) {
//
//    }

    public JsonNode httpPost(String uri, String postContent) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            ObjectMapper objectMapper = new ObjectMapper();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ampurl + uri))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Jerry/1.0")
                    .header("Authorization", "Bearer " + sessionID)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(postContent))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            return null;
        }
    }
}
