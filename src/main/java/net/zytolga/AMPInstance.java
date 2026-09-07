package net.zytolga;

import java.util.List;

public class AMPInstance {
    private final String instanceID;
    private final String friendlyName;
    private final String description;
    private final String serverType;

    private final String ipAddress;
    private final List<Integer> ports;

    private boolean isRunning;
    private int numPlayers;

    private int cpuUsage;
    private int memUsage;
    private final int maxMemory;

    public AMPInstance(String instanceID, String friendlyName, String description, String serverType, String ipAddress, List<Integer> ports, int maxMemory) {
        this.instanceID = instanceID;
        this.friendlyName = friendlyName;
        this.description = description;
        this.serverType = serverType;
        this.ipAddress = ipAddress;
        this.ports = ports;
        this.maxMemory = maxMemory;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public void setCpuUsage(int cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setMemUsage(int memUsage) {
        this.memUsage = memUsage;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getDescription() {
        return description;
    }

    public String getServerType() {
        return serverType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public int getCpuUsage() {
        return cpuUsage;
    }

    public int getMemUsage() {
        return memUsage;
    }

    public int getMaxMemory() {
        return maxMemory;
    }
}
