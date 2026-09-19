package net.zytolga.amp;

import java.util.List;

public class AMPInstance {
    private final String instanceID;
    private final String instanceName;
    private final String friendlyName;
    private final String ampVersion;

    private final String serverType;

    private final String applicationIP;
    private final List<Integer> ports;

    private boolean running;
    private int numPlayers;

    private int cpuUsage;
    private int memUsage;
    private final int maxMemory;

    public AMPInstance(String instanceID, String instanceName, String friendlyName, String ampVersion, String serverType, String ipAddress, List<Integer> ports, int maxMemory) {
        this.instanceID = instanceID;
        this.instanceName = instanceName;
        this.friendlyName = friendlyName;
        this.ampVersion = ampVersion;
        this.serverType = serverType;
        this.applicationIP = ipAddress;
        this.ports = ports;
        this.maxMemory = maxMemory;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getAmpVersion() {
        return ampVersion;
    }

    public String getServerType() {
        return serverType;
    }

    public String getApplicationIP() {
        return applicationIP;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public int getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(int cpuUsage) {
        this.cpuUsage = Math.clamp(cpuUsage, 0, 100);
    }

    public int getMemUsage() {
        return memUsage;
    }

    public void setMemUsage(int memUsage) {
        this.memUsage = Math.clamp(memUsage, 0, 100);
    }

    public int getMaxMemory() {
        return maxMemory;
    }
}
