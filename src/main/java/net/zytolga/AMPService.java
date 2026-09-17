package net.zytolga;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class AMPService {
    private List<AMPInstance> instances;
    private final String url;
    private final String username;
    private final String password;

    public AMPService(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    //Add functionality and HTTP calls
}
