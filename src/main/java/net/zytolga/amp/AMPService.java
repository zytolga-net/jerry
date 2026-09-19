package net.zytolga.amp;

import net.zytolga.Jerry;
import net.zytolga.records.JsonResponse;
import net.zytolga.records.Role;
import net.zytolga.records.StringResponse;
import net.zytolga.records.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AMPService {
    private static final Logger logger = LoggerFactory.getLogger(AMPService.class);

    private final String ampURL;
    private final String username;
    private final String password;
    private String sessionID;
    private SecureRandom secureRandom;

    public AMPService(String ampURL, String username, String password) {
        try {
            secureRandom = SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (NoSuchAlgorithmException e) {
            secureRandom = new SecureRandom();
        }
        this.ampURL = ampURL;
        this.username = username;
        this.password = password;
    }

    @SuppressWarnings("UnusedReturnValue")
    public JsonResponse login() throws Exception {
        String loginBody = """
                    {"USERNAME": "%s","PASSWORD": "%s","token": "","RememberMe": true}
                """.formatted(username, password);

        JsonResponse response = httpPost("/API/Core/Login", loginBody);
        if (!response.success() || !response.body().has("sessionID") || response.body().get("sessionID").asString().trim().isEmpty()) {
            throw new Exception("Invalid session ID returned from AMP");
        }
        sessionID = response.body().get("sessionID").asString();
        return response;
    }

    @SuppressWarnings("UnusedReturnValue")
    public JsonResponse logout() {
        JsonResponse response = httpPost("/API/Core/Logout", "{}");
        if (response.status() == 200) {
            logger.info("Logged out from AMP");
        } else {
            logger.error("Failed to log out from AMP");
        }
        return response;
    }

    public ArrayList<AMPInstance> GetInstances() {
        return GetInstances(false);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ArrayList<AMPInstance> GetInstances(boolean ForceIncludeSelf) {
        String postContent = """
                    {"ForceIncludeSelf": %b}
                """.formatted(ForceIncludeSelf);
        JsonResponse response = httpPost("/API/ADSModule/GetInstances", postContent);
        if (response.success() && response.body().isArray()) {
            ArrayList<AMPInstance> instances = new ArrayList<>();
            response.body().asArray().forEach(adsInstance -> {
                if (!adsInstance.has("AvailableInstances")) {
                    logger.info("ADS instance {} has no available instances", adsInstance.has("FriendlyName") ? adsInstance.get("FriendlyName") : "Unknown");
                } else {
                    adsInstance.get("AvailableInstances").asArray().forEach(instance -> {
                        AMPInstance inst = new AMPInstance(
                                getStringFromJson(instance, "InstanceID"),
                                getStringFromJson(instance, "InstanceName"),
                                getStringFromJson(instance, "FriendlyName"),
                                getStringFromJson(instance, "AMPVersion"),
                                instance.has("ModuleDisplayName") && !instance.get("ModuleDisplayName").isNull() ? getStringFromJson(instance, "ModuleDisplayName") : getStringFromJson(instance, "Module"),
                                getStringFromJson(instance, "IP"),
                                getPortsFromInstance(instance),
                                getMaxMemoryFromInstance(instance)
                        );
                        boolean running = Boolean.TRUE.equals(getBooleanFromJson(instance, "Running"));
                        inst.setRunning(running);
                        inst.setNumPlayers(running ? getNumPlayersFromInstance(instance) : 0);
                        inst.setCpuUsage(running ? getCPUUsageFromInstance(instance) : 0);
                        inst.setMemUsage(running ? getMemoryUsageFromInstance(instance) : 0);
                        instances.add(inst);
                    });
                }
            });
            return instances;
        } else return new ArrayList<>();
    }

    private int getCPUUsageFromInstance(@NotNull JsonNode instance) {
        if (instance.has("Metrics") && !instance.get("Metrics").isNull()) {
            if (instance.get("Metrics").has("CPU Usage") && !instance.get("Metrics").get("CPU Usage").isNull()) {
                if (instance.get("Metrics").get("CPU Usage").has("MaxValue") && !instance.get("Metrics").get("CPU Usage").get("Percent").isNull()) {
                    if (instance.get("Metrics").get("CPU Usage").get("Percent").isInt()) {
                        return instance.get("Metrics").get("CPU Usage").get("Percent").asInt();
                    }
                }
            }
        }
        return 0;
    }

    private int getMemoryUsageFromInstance(@NotNull JsonNode instance) {
        if (instance.has("Metrics") && !instance.get("Metrics").isNull()) {
            if (instance.get("Metrics").has("Memory Usage") && !instance.get("Metrics").get("Memory Usage").isNull()) {
                if (instance.get("Metrics").get("Memory Usage").has("MaxValue") && !instance.get("Metrics").get("Memory Usage").get("Percent").isNull()) {
                    if (instance.get("Metrics").get("Memory Usage").get("Percent").isInt()) {
                        return instance.get("Metrics").get("Memory Usage").get("Percent").asInt();
                    }
                }
            }
        }
        return 0;
    }

    private int getNumPlayersFromInstance(@NotNull JsonNode instance) {
        if (instance.has("Metrics") && !instance.get("Metrics").isNull()) {
            if (instance.get("Metrics").has("Active Users") && !instance.get("Metrics").get("Active Users").isNull()) {
                if (instance.get("Metrics").get("Active Users").has("MaxValue") && !instance.get("Metrics").get("Active Users").get("RawValue").isNull()) {
                    if (instance.get("Metrics").get("Active Users").get("RawValue").isInt()) {
                        return instance.get("Metrics").get("Active Users").get("RawValue").asInt();
                    }
                }
            }
        }
        return 0;
    }

    private int getMaxMemoryFromInstance(JsonNode instance) {
        if (instance.has("Metrics") && !instance.get("Metrics").isNull()) {
            if (instance.get("Metrics").has("Memory Usage") && !instance.get("Metrics").get("Memory Usage").isNull()) {
                if (instance.get("Metrics").get("Memory Usage").has("MaxValue") && !instance.get("Metrics").get("Memory Usage").get("MaxValue").isNull()) {
                    if (instance.get("Metrics").get("Memory Usage").get("MaxValue").isInt()) {
                        return instance.get("Metrics").get("Memory Usage").get("MaxValue").asInt();
                    }
                }
            }
        }
        return 0;
    }

    private List<Integer> getPortsFromInstance(JsonNode instance) {
        List<Integer> ports = new ArrayList<>();
        if (instance.has("ApplicationEndpoints") && !instance.get("ApplicationEndpoints").isNull() && instance.get("ApplicationEndpoints").isArray()) {
            for (JsonNode endpoint : instance.get("ApplicationEndpoints").asArray()) {
                String endpointStr = getStringFromJson(endpoint, "Endpoint");
                if (endpointStr.contains(":")) {
                    ports.add(Integer.valueOf(endpointStr.split(":")[1]));
                }
            }
        }
        return ports;
    }

    private String getStringFromJson(@NotNull JsonNode node, @NotNull String key) {
        return node.has(key) && !node.get(key).isNull() ? node.get(key).asString() : "";
    }

    @Nullable
    private Boolean getBooleanFromJson(@NotNull JsonNode node, @NotNull String key) {
        return node.has(key) && !node.get(key).isNull() && node.get(key).isBoolean() ? node.get(key).asBoolean() : null;
    }

    @SuppressWarnings("unused")
    public AMPInstance GetInstance(String instanceName) {
        AMPInstance[] instances = GetInstances().toArray(new AMPInstance[0]);
        for (AMPInstance instance : instances) {
            logger.info("Instance name {}", instance.getInstanceName());
            if (instance.getInstanceName().equalsIgnoreCase(instanceName)) {
                return instance;
            }
        }
        return null;
    }

    public AMPInstance GetInstanceByFriendlyName(String friendlyName) {
        for (AMPInstance instance : GetInstances()) {
            if (instance.getFriendlyName().equalsIgnoreCase(friendlyName)) {
                return instance;
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public JsonResponse StartInstance(String instanceName) {
        String postContent = """
                    {"InstanceName": "%s"}
                """.formatted(instanceName);
        return httpPost("/API/ADSModule/StartInstance", postContent);
    }

    @SuppressWarnings("unused")
    public JsonResponse RestartInstance(String instanceName) {
        String postContent = """
                    {"InstanceName": "%s"}
                """.formatted(instanceName);
        return httpPost("/API/ADSModule/RestartInstance", postContent);
    }

    @SuppressWarnings("unused")
    public JsonResponse StopInstance(String instanceName) {
        String postContent = """
                    {"InstanceName": "%s"}
                """.formatted(instanceName);
        return httpPost("/API/ADSModule/StopInstance", postContent);
    }

    @SuppressWarnings("UnusedReturnValue")
    public StringResponse AddUser(String username) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#%^&*-_=+";
        String pwd = secureRandom.ints(16, 0, characters.length()).mapToObj(characters::charAt).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();

        StringResponse createUser = CreateUser(username);
        if (!createUser.success()) {
            logger.error("Could not create user for {}", username);
            return new StringResponse("", createUser.status(), false);
        }

        JsonResponse resetPassword = ResetUserPassword(username, pwd);
        if (!resetPassword.success()) {
            logger.error("Could not set user password for {}", username);
            return new StringResponse("", resetPassword.status(), false);
        }

        JsonResponse updateUserInfo = UpdateUserinfo(username, false, false, false, true);

        return new StringResponse(pwd, updateUserInfo.status(), updateUserInfo.success());
    }

    @SuppressWarnings("UnusedReturnValue")
    public StringResponse CreateUser(String username) {
        String postContent = """
                    {"Username": "%s"}
                """.formatted(username);
        JsonResponse response = httpPost("/API/Core/CreateUser", postContent);

        // If the user already exists, override success value to true
        boolean success;
        if (!response.success() && response.body().has("Reason") && response.body().get("Reason").asString().equals("A user with this name already exists."))
            success = true;
        else success = response.success();

        return new StringResponse(response.body().has("Result") ? response.body().get("Result").asString() : "", response.status(), success);
    }

    public JsonResponse ResetUserPassword(String username, String newPassword) {
        String postContent = """
                    {"Username": "%s","NewPassword": "%s"}
                """.formatted(username, newPassword);
        return httpPost("/API/Core/ResetUserPassword", postContent);
    }

    public JsonResponse UpdateUserinfo(String username, boolean disabled, boolean passwordExpires, boolean cannotChangePassword, boolean mustChangePassword) {
        String postContent = """
                    {"Username": "%s","Disabled": %b,"PasswordExpires": %b,"CannotChangePassword": %b,"MustChangePassword": %b}
                """.formatted(username, disabled, passwordExpires, cannotChangePassword, mustChangePassword);
        return httpPost("/API/Core/UpdateUserInfo", postContent);
    }

    @SuppressWarnings("UnusedReturnValue")
    public JsonResponse SetAMPUserRoleMembership(String userName, String roleName, boolean isMember) {
        Optional<User> userOptional = GetUsers().stream().filter(user -> user.name().equalsIgnoreCase(userName)).findFirst();
        User user = userOptional.orElse(null);
        if (user == null || user.id() == null) {
            logger.error("Could not find user with name {}", userName);
            return Jerry.emptyJsonResponseFail;
        }
        String userId = user.id();

        Optional<Role> roleOptional = GetRoles().stream().filter(role -> role.name().equalsIgnoreCase(roleName)).findFirst();
        Role role = roleOptional.orElse(null);
        if (role == null || role.id() == null) {
            logger.error("Could not find role with name {}", roleName);
            return Jerry.emptyJsonResponseFail;
        }
        String roleId = role.id();

        String postContent = """
                    {"UserId": "%s","RoleId": "%s","IsMember": %b}
                """.formatted(userId, roleId, isMember);

        return httpPost("/API/Core/SetAMPUserRoleMembership", postContent);
    }

    public List<User> GetUsers() {
        JsonResponse response = httpPost("/API/Core/GetAllAMPUserInfo", "{}");
        List<User> users = new ArrayList<>();
        if (!response.success() || !response.body().isArray()) {
            logger.error("Could not get list of users");
            return users;
        }
        ArrayNode userArray = response.body().asArray();
        userArray.forEach(user -> users.add(new User(getStringFromJson(user, "Name"), getStringFromJson(user, "ID"), user.has("Disabled") && !user.get("Disabled").isNull() && user.get("Disabled").isBoolean() && user.get("Disabled").asBoolean())));
        return users;
    }

    public List<Role> GetRoles() {
        JsonResponse response = httpPost("/API/Core/GetRoleIds", "{}");
        List<Role> roles = new ArrayList<>();
        if (!response.success()) return roles;
        Collection<String> idList = response.body().asObject().propertyNames();
        for (String roleId : idList) {
            roles.add(new Role(response.body().get(roleId).asString(), roleId));
        }
        return roles;
    }

    @SuppressWarnings("unused")
    public JsonResponse GetAMPUserInfo(String username) {
        String postContent = """
                    {"Username": "%s"}
                """.formatted(username);
        return httpPost("/API/Core/GetAMPUserInfo", postContent);
    }

    public JsonResponse httpPost(String uri, String postContent) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ampURL + uri))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Jerry/1.0")
                    .header("Authorization", "Bearer " + sessionID)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(postContent))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return JsonResponse.fromHttpResponse(response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            return null;
        }
    }
}