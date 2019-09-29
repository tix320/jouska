package com.gitlab.tixtix320.jouska.client.app;


import com.gitlab.tixtix320.jouska.client.service.GameInfoService;
import com.gitlab.tixtix320.sonder.api.client.Clonder;

import java.io.IOException;
import java.util.Collections;

public class Services {
    public static GameInfoService GAME_INFO_SERVICE;

    private static Clonder client;

    public static void initialize(String host, int port) {
        if (client != null) {
            throw new IllegalStateException("CLient already start, maybe you wish reconnect?");
        }
        String servicesPackage = "com.gitlab.tixtix320.jouska.client.service";
        client = Clonder.run(host, port, Collections.singletonList(servicesPackage), Collections.singletonList(servicesPackage));
        initServices();
    }

    public static void stop() throws IOException {
        if (client == null) {
            throw new IllegalStateException("Client does not started");
        }
        client.close();
    }

    private static void initServices() {
        GAME_INFO_SERVICE = client.getService(GameInfoService.class);
    }
}
