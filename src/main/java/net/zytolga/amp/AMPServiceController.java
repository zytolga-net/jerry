package net.zytolga.amp;

public class AMPServiceController {

    public AMPServiceController(String sessionID) throws Exception {
        AMPService ampService = new AMPService("https://amp.zytolga.net", System.getenv("AMP_USERNAME"), System.getenv("AMP_PASSWORD"));
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
