package net.zytolga;

public class AMPServiceController {

    public AMPServiceController(String sessionID) throws Exception {
        AMPService ampService = new AMPService("https://amp.zytolga.net", System.getenv("USERNAME"), System.getenv("PASSWORD"));
        ampService.login();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ampService.logout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
